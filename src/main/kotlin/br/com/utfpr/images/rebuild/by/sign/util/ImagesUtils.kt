package br.com.utfpr.images.rebuild.by.sign.util

import mu.KotlinLogging
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import org.jblas.DoubleMatrix
import org.springframework.web.multipart.MultipartFile
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.sqrt

private val logger = KotlinLogging.logger {}

fun reductionFactor(matrix: RealMatrix): Double {
    return matrix.transpose().multiply(matrix).frobeniusNorm
}

fun regularizationCoefficientCalculus(matrix: RealMatrix, array: RealVector): Double {
    return matrix.operate(array).lInfNorm.times(0.10)
}

fun signalGain(option: Boolean? = true, matrix: RealMatrix): RealMatrix {
    val outputVector = ArrayRealVector()
    if (option == true) {
        val y = ArrayRealVector(S_30_SIGN_GAIN)
        for (i in 1..N_SIGN_GAIN) {
            for (j in 1..S_30_SIGN_GAIN) {
                y.setEntry(j, (HUNDRED.plus(ONE.div(TWENTY).times(j).times(sqrt(j.toDouble())))))
                matrix.setEntry(j, i, matrix.getEntry(j, i) * y.getEntry(j))
            }
        }
    } else {
        val y = ArrayRealVector(S_60_SIGN_GAIN)
        for (i in 1..N_SIGN_GAIN) {
            for (j in 1..S_60_SIGN_GAIN) {
                y.setEntry(j, (HUNDRED.plus(ONE.div(TWENTY).times(j).times(sqrt(j.toDouble())))))

                matrix.setEntry(j, i, matrix.getEntry(j, i) * y.getEntry(j))
            }
        }
    }
    return matrix
}

fun readCsvToDoubleMatrix(csv: MultipartFile): DoubleMatrix {
    val reader = InputStreamReader(csv.inputStream)
    val lines = reader.readLines()
    val realVector = lines.map { it.toDouble() }.toDoubleArray()
    return DoubleMatrix(realVector)
}

fun readCsvToRealVector(csv: MultipartFile): RealVector {
    val reader = InputStreamReader(csv.inputStream)
    val lines = reader.readLines()
    val realVector = lines.map { it.toDouble() }.toDoubleArray()
    return ArrayRealVector(realVector)
}

fun readCsvToRealMatrix(): RealMatrix {
    try {
        val filePath = Paths.get("src", "main", "resources", "csv/H-1.csv").toString()
        val lines = File(filePath).readLines()
        val matrix = Array2DRowRealMatrix(lines.size, lines[0].split(",").size)

        for (i in lines.indices) {
            val cells = lines[i].split(",").map {
                it.toDouble()
            }.toDoubleArray()
            matrix.setRow(i, cells)
        }
        return matrix
    } catch (e: Exception) {
        logger.error(e) {
            "não lê essa porra"
        }
    }
    return MATRIX
}

fun readCsvToDoubleMatrix(): DoubleMatrix {
    val filePath = Paths.get("src", "main", "resources", "csv/H-1.csv").toString()
    val lines = File(filePath).readLines()
    val numRows = lines.size
    val numCols = lines[0].split(",").size

    val matrixData = DoubleMatrix(numRows, numCols)
    var index = 0
    var j = 0

    for ((i, line) in lines.withIndex()) {
        val values = line.split(",")
        for ((j, value) in values.withIndex()) {
            matrixData.put(i, j, value.toDouble())
        }
    }

    return matrixData
}

fun cgne(csv: MultipartFile): ArrayRealVector {
    val realVector = readCsvToRealVector(csv)
    val matrix = readCsvToRealMatrix()

    val n = matrix.columnDimension
    val f0 = ArrayRealVector(n)
    val r0 = realVector.subtract(matrix.operate(f0))
    val p = matrix.transpose().operate(r0)

    var iteration = 0
    var residualNorm = r0.norm

    while (iteration < MAX_ITERATIONS && residualNorm > TOLERANCE) {
        val transposeMatrix = matrix.operate(p)
        val alpha = r0.getEntry(iteration).div((transposeMatrix.dotProduct(transposeMatrix)))
        f0.combineToSelf(1.0, alpha, p)
        r0.combineToSelf(1.0, -alpha, transposeMatrix)

        val residualNormNew = r0.norm
        val beta = r0.dotProduct(transposeMatrix) / (transposeMatrix?.dotProduct(transposeMatrix) ?: 1.0)
        p.combineToSelf(beta, 1.0, p)

        residualNorm = residualNormNew
        iteration++
    }

    return f0
}

fun cgne2(csv: MultipartFile): RealVector {
    val realVector = readCsvToRealVector(csv)
    val matrix = readCsvToRealMatrix()
    val image = matrix.columnDimension

    val f: RealVector = ArrayRealVector(image)
    val r = realVector.subtract(matrix.operate(f))
    val p = matrix.transpose().operate(r)

    for (i in 0 until image) {
        val r = r.copy()
        val alpha = r.ebeMultiply(r).getEntry(i).div(p.ebeMultiply(p).getEntry(i))

        f.setEntry(i, alpha.times(p.getEntry(i)))
        val h = matrix.operate(p)

        r.mapSubtractToSelf(h.mapMultiply(alpha).getEntry(i))

        val beta = r.ebeMultiply(r).getEntry(i).div(r.ebeMultiply(r).getEntry(i))

        val erro = abs(r.norm - r.norm)

        if (erro < TOLERANCE) {
            break
        }
    }
    return f
}

fun saveImage(arrayRealVector: DoubleMatrix?, size: Int, fileName: String) {
    logger.info { "saveImage: start to save image as $fileName" }
    val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)

    for (y in 0 until size) {
        for (x in 0 until size) {
            val pixelIndex = y * size + x
            var pixelValue = arrayRealVector?.get(pixelIndex)?.toInt()
            if (pixelValue != null) {
                if (pixelValue < 0) {
                    pixelValue = 0
                } else if (pixelValue != null) {
                    if (pixelValue > 255) {
                        pixelValue = 255
                    }
                } else pixelValue = pixelValue.times(255.times(255).times(255))
            }
            val pixelColor = Color((pixelValue!!), (pixelValue), (pixelValue))
            pixelColor.rgb.let { image.setRGB(x, y, it) }
        }
    }

    val file = File(FILE_PATH.plus("images/").plus(fileName)).also {
        logger.info { "saveImage: saving $fileName.png in resources/images/" }
    }
    try {
        ImageIO.write(image, "png", file).also {
            logger.info { "Success to convert entry sign to png" }
        }
    } catch (e: Exception) {
        logger.error(e) {
            "Error while converting to png"
        }
    }
}

fun cgneJavaToKotlin(modelMatrix: DoubleMatrix?, entrySign: DoubleMatrix?, imageId: Long): DoubleMatrix? {
    logger.info { "cgneJavaToKotlin: starting CGNE to rebuild image" }
    // ñ conseguiu obter sinal/modelo
    if (modelMatrix == null || entrySign == null) {
        return null
    }
    val f = DoubleMatrix.zeros(3600, 1)
    var norma: Double
    var normaAux = Double.MAX_VALUE
    var alfa: Double
    var beta: Double
    var aux: Double
    var i: Int = 0
    val residue = entrySign.sub(modelMatrix.mmul(f)) // g - H*f
    var p = modelMatrix.transpose().mmul(residue) // Ht * r0
    norma = residue.norm2()
    var aux2: Double
    while (abs(normaAux - norma) > TOLERANCE) {
        aux = residue.transpose().mmul(residue)[0]
        aux2 = p.transpose().mmul(p)[0]
        alfa = aux.div(aux2) // (Rt*R)/(Pt*P)

        f.put(i, (f[i].plus(p[i].times(alfa)))) // f = f + a*p -> addi (in-place)
        residue.subi(modelMatrix.mmul(alfa).mmul(p)) // r i+1 = r - a*H*p
        beta = residue.transpose().mmul(residue)[0] / aux // (Rt*R)/(Rt*R)
        p = modelMatrix.transpose().mmul(residue).add(p.mmul(beta)) // Ht*r + b*p
        normaAux = norma // salva norma anterior
        norma = residue.norm2() // atualiza a norma
        i++
        if (i >= 1000) {
            return p
        } // limite de iterações
    }
    logger.info { "cgneJavaToKotlin: end of image processing and rebuilding" }
    return p
}

fun saveImageFromDoubleMatrix(doubleMatrix: DoubleMatrix, size: Int, filePath: String) {
    val width = doubleMatrix.columns
    val height = doubleMatrix.rows

    // Criar uma nova imagem BufferedImage
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    // Preencher a imagem com os valores da matriz
    for (x in 0 until size) {
        for (y in 0 until size) {
            val value = doubleMatrix.get(y, x).toInt() // Converter para inteiro
            val rgb = value shl 16 or (value shl 8) or value // Converter para RGB
            image.setRGB(x, y, rgb)
        }
    }

    // Salvar a imagem em um arquivo
    try {
        val file = File(FILE_PATH.plus("images/").plus(filePath))
        ImageIO.write(image, "PNG", file)
        println("Imagem salva com sucesso: $filePath")
    } catch (e: IOException) {
        println("Erro ao salvar a imagem: ${e.message}")
    }
}

const val MAX_ITERATIONS = 1000
const val TOLERANCE = 1e-4
const val N_SIGN_GAIN = 64
const val S_30_SIGN_GAIN = 436
const val S_60_SIGN_GAIN = 794
const val ONE = 1
const val TWENTY = 20
const val HUNDRED = 100
const val FILE_PATH = "src/main/resources/"
const val PYTHON_PATH = "src/main/python/main.py"
val MATRIX: RealMatrix = Array2DRowRealMatrix(
    arrayOf(
        doubleArrayOf(1.0, 1.0),
        doubleArrayOf(1.0, 1.0)
    )
)
