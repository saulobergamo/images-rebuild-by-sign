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
