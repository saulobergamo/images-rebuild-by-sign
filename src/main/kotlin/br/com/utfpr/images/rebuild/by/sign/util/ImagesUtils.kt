package br.com.utfpr.images.rebuild.by.sign.util

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
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

fun reductionFactor(matrix: RealMatrix): Double {
    return matrix.transpose().multiply(matrix).frobeniusNorm
}

fun regularizationCoefficientCalculus(matrix: RealMatrix, array: RealVector): Double {
    return matrix.operate(array).lInfNorm.times(0.10)
}
fun readCsvToRealVector(csv: MultipartFile): RealVector {
    val reader = InputStreamReader(csv.inputStream)
    val lines = reader.readLines()
    val realVector = lines.map { it.toDouble().roundToInt().toDouble() }.toDoubleArray()
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
    val maxIterations = 1000
    val tolerance = 1e-4
    val realVector = readCsvToRealVector(csv)
    val matrix = readCsvToRealMatrix()

    val n = matrix.columnDimension
    val x = ArrayRealVector(n)
    val r = realVector.subtract(matrix.operate(x))
    val p = r.copy()

    var iteration = 0
    var residualNorm = r.norm

    while (iteration < maxIterations && residualNorm > tolerance) {
        val ap = matrix.operate(p)
        val alpha: Double = r.dotProduct(ap) / (ap.dotProduct(ap))
        x.combineToSelf(1.0, alpha, p)
        r.combineToSelf(1.0, -alpha, ap)

        val residualNormNew = r.norm
        val beta = r.dotProduct(ap) / (ap?.dotProduct(ap) ?: 1.0)
        p.combineToSelf(beta, 1.0, p)

        residualNorm = residualNormNew
        iteration++
    }

    return x
}

fun saveImage(arrayRealVector: ArrayRealVector, size: Int, filePath: String) {
    val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)

    for (y in 0 until size) {
        for (x in 0 until size) {
            val pixelIndex = y * size + x
            val pixelValue = arrayRealVector.getEntry(pixelIndex)
            val pixelColor = Color((pixelValue * 255).toInt(), (pixelValue * 255).toInt(), (pixelValue * 255).toInt())
            image.setRGB(x, y, pixelColor.rgb)
        }
    }

    val file = File(FILE_PATH.plus("images/").plus(filePath))
    try {
        ImageIO.write(image, "png", file)
    } catch (e: Exception) {
        logger.error(e) {
            "Não gravou essa bosta"
        }
    }
}

const val FILE_PATH = "src/main/resources/"
val MATRIX: RealMatrix = Array2DRowRealMatrix(
    arrayOf(
        doubleArrayOf(1.0, 1.0),
        doubleArrayOf(1.0, 1.0)
    )
)
