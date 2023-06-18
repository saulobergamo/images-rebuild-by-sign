package br.com.utfpr.images.rebuild.by.sign.util

import br.com.utfpr.images.rebuild.by.sign.enums.ImageSize
import mu.KotlinLogging
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import org.springframework.web.multipart.MultipartFile
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
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
        val filePath = Paths.get("src", "main", "resources", "H-1.csv").toString()
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

fun saveImage(arrayRealVector: RealVector, size: ImageSize, fileName: String) {
    val image = BufferedImage(size.size, size.size, BufferedImage.TYPE_INT_RGB)

    for (y in 0 until size.size) {
        for (x in 0 until size.size) {
            val pixelIndex = y * size.size + x
            val pixelValue = arrayRealVector.getEntry(pixelIndex)
            val pixelColor = Color((pixelValue * 255).toInt(), (pixelValue * 255).toInt(), (pixelValue * 255).toInt())
            image.setRGB(x, y, pixelColor.rgb)
        }
    }

    val file = File(FILE_PATH.plus("images/").plus(fileName))
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

const val MAX_ITERATIONS = 1000
const val TOLERANCE = 1e-4
const val N_SIGN_GAIN = 64
const val S_30_SIGN_GAIN = 436
const val S_60_SIGN_GAIN = 794
const val ONE = 1
const val TWENTY = 20
const val HUNDRED = 100
const val FILE_PATH = "src/main/resources/"
val MATRIX: RealMatrix = Array2DRowRealMatrix(
    arrayOf(
        doubleArrayOf(1.0, 1.0),
        doubleArrayOf(1.0, 1.0)
    )
)
