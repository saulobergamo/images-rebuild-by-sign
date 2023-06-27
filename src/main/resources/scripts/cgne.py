# -*- coding: utf-8 -*-
import numpy as np
import pandas as pd
import cv2 as cv
import os
import sys
import json
import pymongo

PATH = os.path.expanduser("~/utfpr/Desenvolvimento Integrado de Sistemas/images-rebuild-by-sign/src/main/resources/")

def run_cgne(key):
    client = pymongo.MongoClient("mongodb://admin:admin@localhost:27017/")
    db = client['test']
    collection = db['entry_sign_to_rebuild_image']

    # find one document
    entry = collection.find_one({"clientId":key})
    r = entry['entrySign']
    matriz = np.load(PATH + "pickle/H-1.pickle", allow_pickle=True)
    matriz = np.asarray(matriz, dtype=np.float64)

    # r0=g−Hf0
    # r = np.loadtxt(PATH + "G-2.csv", delimiter=',', dtype=np.float64)
    r.shape = (r.shape[0], 1)

    # p0=HTr0
    p = np.matmul(matriz.T, r)

    # f0=0
    image = np.zeros_like(len(p))

    erro = 0
    while erro < 1e10-4:
        # αi=rTiripTipi
        alpha = np.dot(r.T, r) / np.dot(p.T, p)

        # fi+1=fi+αipi
        image = image + alpha * p.T

        # ri+1=ri−αiHpi
        r_aux = r - alpha * np.dot(matriz, p)

        # ϵ=ri+12−ri2
        erro = np.linalg.norm(r_aux) - np.linalg.norm(r)
        if erro < 1e10-4:
                break

        # βi=rTi+1ri+1rTiri
        beta = np.dot(r_aux.T, r_aux) / np.dot(r.T, r)

        # pi+1=HTri+1+βipi
        p = np.dot(matriz.T, r_aux) + beta * p

    image = image - image.min()
    image = image / image.max()
    image = image * 255

    image = image.reshape((60, 60), order='F')

    res = cv.resize(image, None, fx=10, fy=10, interpolation=cv.INTER_NEAREST)

    cv.imwrite(PATH + 'images/testeHashId.png', res)


if __name__ == '__main__':
    key = sys.argv[1]
    run_cgne(key)