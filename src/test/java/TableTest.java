import io.github.hzkitty.RapidOCR;
import io.github.hzkitty.entity.OcrResult;
import io.github.hzkitty.entity.ParamConfig;
import io.github.hzkitty.rapidtable.RapidTable;
import io.github.hzkitty.rapidtable.entity.TableResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

public class TableTest {

    @Test
    public void testPath() throws Exception {
        RapidTable tableEngine = RapidTable.create();
        RapidOCR rapidOCR = RapidOCR.create();

        File file = new File("src/test/resources/table_01.jpg");
        String imgContent = file.getAbsolutePath();
        OcrResult ocrResult = rapidOCR.run(imgContent);
        TableResult tableResult = tableEngine.run(imgContent, ocrResult.getRecRes());
        Assertions.assertFalse(tableResult.getCellBoxes().isEmpty());
        System.out.println(tableResult);
    }

    @Test
    public void testBufferedImage() throws Exception {
        RapidTable tableEngine = RapidTable.create();
        RapidOCR rapidOCR = RapidOCR.create();
        File file = new File("src/test/resources/table_01.jpg");
        BufferedImage imgContent = ImageIO.read(file);

        ParamConfig paramConfig = new ParamConfig();
        paramConfig.setReturnWordBox(true);
        OcrResult ocrResult = rapidOCR.run(imgContent, paramConfig);
        TableResult tableResult = tableEngine.run(imgContent, ocrResult.getRecRes());
        Assertions.assertFalse(tableResult.getCellBoxes().isEmpty());
        System.out.println(tableResult);
    }

    @Test
    public void testByte() throws Exception {
        RapidTable tableEngine = RapidTable.create();
        RapidOCR rapidOCR = RapidOCR.create();
        File file = new File("src/test/resources/table_01.jpg");
        byte[] imgContent = Files.readAllBytes(file.toPath());
        OcrResult ocrResult = rapidOCR.run(imgContent);
        TableResult tableResult = tableEngine.run(imgContent, ocrResult.getRecRes());
        Assertions.assertFalse(tableResult.getCellBoxes().isEmpty());
        System.out.println(tableResult);
    }

    @Test
    public void testMat() throws Exception {
        RapidTable tableEngine = RapidTable.create();
        RapidOCR rapidOCR = RapidOCR.create();
        File file = new File("src/test/resources/table_01.jpg");
        Mat imgContent = Imgcodecs.imread(file.getAbsolutePath());
        OcrResult ocrResult = rapidOCR.run(imgContent);
        TableResult tableResult = tableEngine.run(imgContent, ocrResult.getRecRes());
        Assertions.assertFalse(tableResult.getCellBoxes().isEmpty());
        System.out.println(tableResult);
    }

}
