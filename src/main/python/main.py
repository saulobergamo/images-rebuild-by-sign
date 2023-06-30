import base64
import random
import sys
import numpy as np
import cv2 as cv
import time
import matplotlib.pyplot as plt
import psutil
import pymongo
from scipy.ndimage import maximum_filter
import os
import threading

PATH = os.path.expanduser("~/utfpr/Desenvolvimento Integrado de Sistemas/images-rebuild-by-sign/src/main/resources/")


def get_sign_from_DB(image_id):
    client = pymongo.MongoClient("mongodb://admin:admin@localhost:27017/")
    db = client['BSI-DIS']
    collection = db['entry_sign_to_rebuild_image']

    # find one document
    entry = collection.find_one({"imageId": image_id})
    response = entry['entrySignDouble']
    response = np.array(response, dtype=np.float64)
    response.shape = (response.shape[0], 1)
    return response


def save_image(data):
    client = pymongo.MongoClient("mongodb://admin:admin@localhost:27017/")
    db = client['BSI-DIS']
    collection = db['image_rebuild']
    collection.insert_one(data)


def normalize(img, signal):
    if signal.shape == (50816, 1):
        if signal[96] == -9.408935e-13:
            treshold = 0.67
            difference = 23
            skip = 10
        elif signal[95] == -9.4462e-06:
            treshold = 0.88
            difference = 18
            skip = 5
        else:
            treshold = 1
            difference = 37
            skip = -1
        treshold = 1
        difference = 45
        skip = -1
    else:
        if signal[80] == 14900:
            treshold = 1
            difference = 50
            skip = -1
        elif signal[80] == -1.1188e-13:
            treshold = 1
            difference = 45
            skip = -1
        else:
            treshold = 1
            difference = 45
            skip = -1

    m = np.max(img, axis=1)
    n = np.min(img, axis=1)
    for i in range(len(img[0])):
        for j in range(len(img[1])):
            if m[i] - n[i] > difference and (i + 1) % skip == 0:
                if img[i][j] < treshold * m[i]:
                    img[i][j] = 0
            else:
                img[i][j] = 0
    return img


def signal_gain(sign_type):
    N = 64
    S1 = 794
    S = 436

    if (sign_type == "true"):
        S = S1

    gl = 0
    for c in range(0, S):
        for l in range(0, N):
            gamma = 100 + (1 / 20) * l * (l ** 0.5)
            gl, c = gl, c * gamma

def load_model_matrix(matrix_name):
    matrix = np.load(PATH + f'pickle/{matrix_name}.pickle', allow_pickle=True)
    matrix = np.asarray(matrix, dtype=np.float64)
    return matrix

def get_matrix_model_name(sign_type):
    if sign_type == 'true':
        matriz_name = 'H-1'
    else:
        matriz_name = 'H-2'
    return matriz_name 

def image_reshape(image, sign_type):
    image = image - image.min()
    image = image / image.max()
    image = image * 255
    if sign_type == 'true':
        image = image.reshape((60, 60), order='F')
    else:
        image = image.reshape((30, 30), order='F')
    return image

def cgne(image_id, sign_type, user_name, algorithm):
    global max_cpu_usage
    global v
    start_time = time.time()

    # r0=g−Hf0
    entry_sign = get_sign_from_DB(image_id)
    matrix = load_model_matrix(get_matrix_model_name(sign_type))

    # p0=HTr0
    p = np.matmul(matrix.T, entry_sign)

    # f0=0
    image = np.zeros_like(len(p))

    count = 1
    error = 0
    while error < float(1e10**(-4)):
        # αi=rTiripTipi
        alpha = np.dot(entry_sign.T, entry_sign) / np.dot(p.T, p)

        # fi+1=fi+αipi
        image = image + alpha * p.T

        # ri+1=ri−αiHpi
        ri = entry_sign - alpha * np.dot(matrix, p)

        # ϵ=||ri+1||2−||ri||2
        error += np.linalg.norm(entry_sign, ord=2) - np.linalg.norm(ri, ord=2)
        if error < 1e10 - 4:
            break

        # βi=rTi+1ri+1rTiri
        beta = np.dot(entry_sign.T, ri) / np.dot(ri.T, entry_sign)

        # pi+1=HTri+1+βipi
        p = np.dot(matrix.T, ri) + beta * p

        count += 1

    v = False
    time.sleep(0.25)  

    image = image_reshape(image, sign_type)
    normalized_image = normalize(image, entry_sign)
    # image_array_list = normalized_image.tolist()
    image_array_list = image.tolist()
    process = psutil.Process()
    memory = process.memory_info().rss / 1000000
    run_time = time.time() - start_time
 
    data = {
        "userName": user_name,
        "imageId": image_id,
        "iterations": count,
        "runTime": run_time,
        "error": error,
        "memory": memory,
        "signType": sign_type,
        "algorithm": algorithm,
        "cpu": max_cpu_usage,
        "image": image_array_list
    }
    memory = process.memory_info().rss / 1000000
    # plt.imshow(normalized_image, cmap='gray')
    # plt.title('CGNE - Log')
    # plt.show()

    # # Salvar imagem localmente
    #     cv.imwrite(PATH+ 'images/teste2.png', final)

    # Salvar imagem no banco MONGODB
    save_image(data)

def cgnr(image_id, sign_type, user_name, algorithm):
    global max_cpu_usage
    global v
    start_time = time.time()
    entry_sign = get_sign_from_DB(image_id)
    matrix = load_model_matrix(get_matrix_model_name(sign_type))

    # z0=HTr0
    p = np.matmul(matrix.T, entry_sign)
    z = p

    # f0=0
    image = np.zeros_like(len(p))

    count = 1
    erro = 0
    while erro < 1e10**(-4):
        # wi=Hpi
        w = np.matmul(matrix, p)

        # αi=||zi||22/||wi||22
        alpha = np.linalg.norm(z, ord=2) ** 2 / np.linalg.norm(w, ord=2) ** 2

        # fi+1=fi+αipi
        image = image + alpha * p.T

        # ri+1=ri−αiwi
        ri = entry_sign - alpha * w

        # zi+1=HTri+1
        z = np.matmul(matrix.T, ri)

        # βi=||zi+1||22/||zi||22
        beta = np.linalg.norm(z, ord=2) ** 2 / np.linalg.norm(z, ord=2) ** 2

        # pi+1=zi+1+βipi
        p = z + beta * p

        # ϵ=||ri+1||2−||ri||2
        erro = np.linalg.norm(ri, ord=2) - np.linalg.norm(entry_sign, ord=2)
        if erro < 1e10**(-4):
            break

        count += 1

    image = image_reshape(image, sign_type)
    normalized_image = normalize(image, entry_sign)
    # image_array_list = normalized_image.tolist()
    image_array_list = image.tolist()
    process = psutil.Process()
    memory = process.memory_info().rss / 1000000
    run_time = time.time() - start_time

    v = False

    time.sleep(0.2)

    data = {
        "userName": user_name,
        "imageId": image_id,
        "iterations": count,
        "runTime": run_time,
        "error": erro,
        "memory": memory,
        "signType": sign_type,
        "algorithm": algorithm,
        "cpu": max_cpu_usage,
        "image": image_array_list
    }
    memory = process.memory_info().rss / 1000000
    save_image(data)
    # final = cv.resize(image, None, fx=10, fy=10, interpolation=cv.INTER_AREA)
    # cv.imwrite(PATH + 'images/testecgnr.png', final)

def main(image_id, sign_type, user_name):
    t = threading.Thread(target=monitor_cpu_usage)
    t.start()

    algorithms = ['cgne', 'cgnr']
    algorithm = random.choice(algorithms)
    if algorithm == 'cgne':
        # cgne(image_id, sign_type, user_name, algorithm)
        t2 = threading.Thread(target=cgne, args=(image_id, sign_type, user_name, algorithm))
        t2.start()
        # t2.join()
    else:
        # cgnr(image_id, sign_type, user_name, algorithm)
        t2 = threading.Thread(target=cgne, args=(image_id, sign_type, user_name, algorithm))
        t2.start()
    t2.join()
    t.join()


def monitor_cpu_usage():
    global v
    global max_cpu_usage

    v = True
    max_cpu_usage = 0

    cpu_usage_list_by_second = []
    while v:
        cpu_usage_list_by_second.append(psutil.cpu_percent(interval=0.25))
    max_cpu_usage = max(cpu_usage_list_by_second)


if __name__ == '__main__':
    image_id = sys.argv[1]
    sign_type = sys.argv[2]
    user_name = sys.argv[3]

    main(image_id, sign_type, user_name)
