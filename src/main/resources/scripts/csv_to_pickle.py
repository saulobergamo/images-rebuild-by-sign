import pandas as pd

matrix_path = "~/utfpr/Desenvolvimento Integrado de Sistemas/images-rebuild-by-sign/src/main/resources/csv/G-1.csv"
matrix = pd.read_csv(matrix_path, header=None)
matrix.to_pickle("~/utfpr/Desenvolvimento Integrado de Sistemas/images-rebuild-by-sign/src/main/resources/pickle/G-1.pickle")