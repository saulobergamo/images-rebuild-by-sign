import base64
import sys
import numpy as np
import cv2 as cv
import time
import matplotlib.pyplot as plt
import psutil
import pymongo
from scipy.ndimage import maximum_filter
import os


PATH = os.path.expanduser("~/utfpr/Desenvolvimento Integrado de Sistemas/images-rebuild-by-sign/src/main/resources/")


def get_sign_from_DB(key):
    client = pymongo.MongoClient("mongodb://admin:admin@localhost:27017/")
    db = client['test']
    collection = db['entry_sign_to_rebuild_image']

    # find one document
    entry = collection.find_one({"clientId":key})
    return entry['entrySignDouble']

def save_image(data):
    client = pymongo.MongoClient("mongodb://admin:admin@localhost:27017/")
    db = client['test']
    collection = db['image_rebuild']
    collection.insert_one(data)

def normalize(img):
    m = np.max(img, axis=1)
    m = maximum_filter(m, size=5)
    img_normalized = img / m[:, np.newaxis]
    output = img_normalized > 0.73
    output = output * img
    return output


def cgne(key):
    start_time = time.time()

    matriz = np.load(PATH + 'pickle/H-1.pickle', allow_pickle=True)
    matriz = np.asarray(matriz, dtype=np.float64)

    # N = 794, S = 64
    # for c=1 .. N
    #   for l=1 .. S
    #       γl=100+1/20∗l∗√l
    #       gl,c=gl,c∗γl
    gl = 0
    for c in range(0, 794):
        for l in range(0, 64):
            gamma = 100 + (1 / 20) * l * (l ** 0.5)
            gl, c = gl, c * gamma

    # r0=g−Hf0
    # r = np.loadtxt(PATH + 'csv/G-1.csv', delimiter=',',
                #    dtype=np.float64)
    r = get_sign_from_DB(key)
    r = np.array(r, dtype=np.float64)
    r.shape = (r.shape[0], 1)

    # p0=HTr0
    p = np.matmul(matriz.T, r)

    # f0=0
    image = np.zeros_like(len(p))

    count = 1
    erro = 0
    while erro < float(1e10 - 4):
        # αi=rTiripTipi
        alpha = np.dot(r.T, r) / np.dot(p.T, p)

        # fi+1=fi+αipi
        image = image + alpha * p.T

        # ri+1=ri−αiHpi
        ri = r - alpha * np.dot(matriz, p)

        # ϵ=||ri+1||2−||ri||2
        erro += np.linalg.norm(r, ord=2) - np.linalg.norm(ri, ord=2)
        if erro < 1e10 - 4:
            break

        # βi=rTi+1ri+1rTiri
        beta = np.dot(r.T, ri) / np.dot(ri.T, r)

        # pi+1=HTri+1+βipi
        p = np.dot(matriz.T, ri) + beta * p

        count += 1

    image = image - image.min()
    image = image / image.max()
    image = image * 255

    image = image.reshape((60, 60), order='F')

    # normalization
    # image = normalize(image)

    a = normalize(image)

# Mostra a imagem gerada
#     plt.imshow(a, cmap='gray')
#     plt.title('Log')
#     plt.show()

    final = cv.resize(image, None, fx=10, fy=10, interpolation=cv.INTER_AREA)

    run_time = time.time() - start_time
    image_array_list = image.tolist()
    process = psutil.Process()
    memory = process.memory_info().rss / 1000000

    data = {
        "clientId": key,
        "iterations": count,
        "runTime": run_time,
        "error": erro,
        "memory": memory,
        "image": image_array_list,
#         "process": process,
    }

#     print(count)
#     print(run_time)
#     print(str(memory) + " MB")
#     print(erro)
#     plt.imshow(image, cmap='gray')
#     plt.title('Log')
#     plt.show()
    # Salvar imagem localmente
#     cv.imwrite(PATH+ 'images/teste2.png', final)

    # Salvar imagem no banco MONGODB
    save_image(data)


def cgnr():
    start_time = time.time()

    matriz = np.load(PATH + 'pickle/H-1.pickle', allow_pickle=True)
    matriz = np.asarray(matriz, dtype=np.float64)

    # N = 794, S = 64
    # for c=1 .. N
    #   for l=1 .. S
    #       γl=100+1/20∗l∗√l
    #       gl,c=gl,c∗γl
    gl = 0
    for c in range(0, 794):
        for l in range(0, 64):
            gamma = 100 + (1 / 20) * l * (l ** 0.5)
            gl, c = gl, c * gamma

    # r0=g−Hf0
    r = np.loadtxt(PATH + 'csv/G-2.csv', delimiter=',',
                   dtype=np.float64)
    r.shape = (r.shape[0], 1)

    # z0=HTr0
    p = np.matmul(matriz.T, r)
    z = p

    # f0=0
    image = np.zeros_like(len(p))

    count = 1
    erro = 0
    while erro < 1e10 - 4:
        # wi=Hpi
        w = np.matmul(matriz, p)

        # αi=||zi||22/||wi||22
        alpha = np.linalg.norm(z, ord=2) ** 2 / np.linalg.norm(w, ord=2) ** 2

        # fi+1=fi+αipi
        image = image + alpha * p.T

        # ri+1=ri−αiwi
        ri = r - alpha * w

        # zi+1=HTri+1
        z = np.matmul(matriz.T, ri)

        # βi=||zi+1||22/||zi||22
        beta = np.linalg.norm(z, ord=2) ** 2 / np.linalg.norm(z, ord=2) ** 2

        # pi+1=zi+1+βipi
        p = z + beta * p

        # ϵ=||ri+1||2−||ri||2
        erro = np.linalg.norm(ri, ord=2) - np.linalg.norm(r, ord=2)
        if erro < 1e10 - 4:
            break

        count += 1

    image = image - image.min()
    image = image / image.max()
    image = image * 255

    image = image.reshape((60, 60), order='F')

    final = cv.resize(image, None, fx=10, fy=10, interpolation=cv.INTER_AREA)

    cv.imwrite(PATH + 'images/testecgnr.png', final)
    run_time = time.time() - start_time
    print(count)
    print(run_time)

    return image, count, run_time


def main(key):
    cgne(key)


if __name__ == '__main__':
    key = sys.argv[1]
    main(key)
