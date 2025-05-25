import io.github.hzkitty.RapidOCR;
import io.github.hzkitty.entity.OcrResult;
import io.github.hzkitty.rapidtable.RapidTable;
import io.github.hzkitty.rapidtable.entity.TableResult;
import io.github.hzkitty.rapidtable.utils.VisTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TableVisTest {

    @Test
    public void testTable() throws Exception {
        RapidTable tableEngine = RapidTable.create();
        RapidOCR ocrEngine = RapidOCR.create();
        VisTable viser = new VisTable();

        File file = new File("src/test/resources/table_01.jpg");
        String imgPath = file.getAbsolutePath();
        OcrResult ocrResult = ocrEngine.run(imgPath);

        TableResult table = tableEngine.run(imgPath, ocrResult.getRecRes(), true);

        Assertions.assertFalse(table.getCellBoxes().isEmpty());
        System.out.println(table.getHtmlStr());
        System.out.println(table);

        Path saveDir = Paths.get("src/test/resources/inference_results").toAbsolutePath();

        Files.createDirectories(saveDir);

        // 获取文件的stem（不含扩展名）
        String stem = getFileStem(imgPath);

        // 构建保存HTML的路径
        Path saveHtmlPath = saveDir.resolve(stem + ".html");

        // 获取输入图片的文件名
        Path inputPath = Paths.get(imgPath);
        String inputFileName = inputPath.getFileName().toString();

        // 构建保存绘制后图片的路径
        Path saveDrawedPath = saveDir.resolve("vis_" + inputFileName);

        viser.call(imgPath, table.getHtmlStr(), saveHtmlPath.toString(), table.getCellBoxes(), saveDrawedPath.toString(), null, null);

        Path saveLogicPath = saveDir.resolve("vis_logic_" + inputFileName);
        viser.call(imgPath, table.getHtmlStr(), saveHtmlPath.toString(), table.getCellBoxes(), saveDrawedPath.toString(), table.getLogicPoints(), saveLogicPath.toString());
    }


    private static String getFileStem(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }
}
