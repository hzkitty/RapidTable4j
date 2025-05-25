package io.github.hzkitty.rapidtable;

import io.github.hzkitty.entity.*;
import io.github.hzkitty.rapidtable.entity.TableConfig;
import io.github.hzkitty.rapidtable.entity.TableModelType;
import io.github.hzkitty.rapidtable.entity.TableResult;
import io.github.hzkitty.rapidtable.tablematcher.TableMatch;
import io.github.hzkitty.rapidtable.tablestructure.TableStructurer;
import io.github.hzkitty.rapidtable.utils.LoadImage;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RapidTable {

    private final TableModelType modelType;
    private final LoadImage loadImg;
    private final TableStructurer tableStructure;
    private final TableMatch tableMatcher;

    public static RapidTable create() {
        return new RapidTable();
    }

    public static RapidTable create(TableConfig config) {
        return new RapidTable(config);
    }

    public RapidTable() {
        this(new TableConfig());
    }

    public RapidTable(TableConfig config) {
        this.modelType = config.modelType;
        // 初始化 LoadImage
        this.loadImg = new LoadImage();

        OrtInferConfig inferConfig = new OrtInferConfig();
        inferConfig.setModelPath(config.modelPath);
        inferConfig.setUseCuda(config.useCuda);
        inferConfig.setDeviceId(config.deviceId);
        inferConfig.setUseArena(true);

        // 1. 初始化表格结构识别器
        this.tableStructure = new TableStructurer(inferConfig);

        // 2. 初始化表格匹配器
        this.tableMatcher = new TableMatch();
    }

    public TableResult run(String imagePath, List<RecResult> ocrResult) throws Exception {
        return this.runImpl(imagePath, ocrResult, false);
    }

    public TableResult run(Path imagePath, List<RecResult> ocrResult) throws Exception {
        return this.runImpl(imagePath, ocrResult, false);
    }

    public TableResult run(byte[] imageData, List<RecResult> ocrResult) throws Exception {
        return this.runImpl(imageData, ocrResult, false);
    }

    public TableResult run(BufferedImage image, List<RecResult> ocrResult) throws Exception {
        return this.runImpl(image, ocrResult, false);
    }

    public TableResult run(Mat mat, List<RecResult> ocrResult) throws Exception {
        return this.runImpl(mat, ocrResult, false);
    }

    public TableResult run(String imagePath, List<RecResult> ocrResult, boolean returnLogicPoints) throws Exception {
        return this.runImpl(imagePath, ocrResult, returnLogicPoints);
    }

    public TableResult run(Path imagePath, List<RecResult> ocrResult, boolean returnLogicPoints) throws Exception {
        return this.runImpl(imagePath, ocrResult, returnLogicPoints);
    }

    public TableResult run(byte[] imageData, List<RecResult> ocrResult, boolean returnLogicPoints) throws Exception {
        return this.runImpl(imageData, ocrResult, returnLogicPoints);
    }

    public TableResult run(BufferedImage image, List<RecResult> ocrResult, boolean returnLogicPoints) throws Exception {
        return this.runImpl(image, ocrResult, returnLogicPoints);
    }

    public TableResult run(Mat mat, List<RecResult> ocrResult, boolean returnLogicPoints) throws Exception {
        return this.runImpl(mat, ocrResult, returnLogicPoints);
    }


    private TableResult runImpl(Object imgContent, List<RecResult> ocrResult, boolean returnLogicPoints) throws Exception {
        // 1. 加载图像
        Mat img = this.loadImg.call(imgContent);

        long startTime = System.currentTimeMillis();
        int h = img.rows();
        int w = img.cols();

        // 2. 解析 dt_boxes, rec_res
        Pair<List<float[]>, List<Pair<String, Float>>> boxAndRec = getBoxesRecs(ocrResult, h, w);
        List<float[]> dtBoxes = boxAndRec.getLeft();
        List<Pair<String, Float>> recRes = boxAndRec.getRight();

        // 3. 表格结构推理: pred_structures, pred_bboxes, ...
        Triple<List<String>, List<float[]>, Double> structureRes = this.tableStructure.call(img);
        List<String> predStructures = structureRes.getLeft();
        List<float[]> predBBoxes = structureRes.getMiddle();

        // 4、如果是 slanet-plus，需要缩放
        if (TableModelType.SLANET_PLUS.equals(this.modelType)) {
            predBBoxes = adaptSlanetPlus(img, predBBoxes);
        }

        // 5. 调用表格匹配器, 组装最终 HTML
        String predHtml = this.tableMatcher.call(predStructures, predBBoxes, dtBoxes, recRes);
        // 6. 若需要逻辑坐标
        double elapse = (System.currentTimeMillis() - startTime) / 1000.0;
        if (returnLogicPoints) {
            List<int[]> logicPoints = this.tableMatcher.decodeLogicPoints(predStructures);
            return new TableResult(predHtml, predBBoxes, logicPoints, elapse);
        }
        return new TableResult(predHtml, predBBoxes, null, elapse);
    }

    /**
     * 处理OCR结果，获取边框和识别结果
     *
     * @param ocrResults OCR结果列表
     * @param h 图片高度
     * @param w 图片宽度
     * @return 包含边框和识别结果的BoxRecResult对象
     */
    public Pair<List<float[]>, List<Pair<String, Float>>> getBoxesRecs(List<RecResult> ocrResults, int h, int w) {
        List<float[]> rBoxes = new ArrayList<>();
        List<Pair<String, Float>> recRes = ocrResults.stream()
                .map(rec -> Pair.of(rec.getText(), rec.getConfidence()))
                .collect(Collectors.toList());

        for (RecResult result : ocrResults) {
            // 处理识别结果和分数

            // 处理边框坐标
            Point[] box = result.getDtBoxes();
            if (box == null || box.length == 0) {
                continue; // 如果边框为空，跳过
            }

            float xMin = Float.MAX_VALUE;
            float xMax = Float.MIN_VALUE;
            float yMin = Float.MAX_VALUE;
            float yMax = Float.MIN_VALUE;

            for (Point point : box) {
                float x = (float) point.x;
                float y = (float) point.y;
                if (x < xMin) xMin = x;
                if (x > xMax) xMax = x;
                if (y < yMin) yMin = y;
                if (y > yMax) yMax = y;
            }

            // 调整边界并确保在图片范围内
            xMin = Math.max(0, xMin - 1);
            xMax = Math.min(w, xMax + 1);
            yMin = Math.max(0, yMin - 1);
            yMax = Math.min(h, yMax + 1);

            rBoxes.add(new float[]{xMin, yMin, xMax, yMax});
        }
        return Pair.of(rBoxes, recRes);
    }

    /**
     * 适配 slanet-plus 输出 box 缩放
     *
     * @param img         图像对象
     * @param predBBoxes  原预测 box, shape [n, 8] or [n, 4] 需看项目情况
     * @return 适配后的 box
     */
    private List<float[]> adaptSlanetPlus(Mat img, List<float[]> predBBoxes) {
        int h = img.rows();
        int w = img.cols();

        int resized = 488;
        float ratio = Math.min((float) resized / h, (float) resized / w);

        float w_ratio = resized / (w * ratio);
        float h_ratio = resized / (h * ratio);

        for (int i = 0; i < predBBoxes.size(); i++) {
            for (int j = 0; j < predBBoxes.get(i).length; j++) {
                if (j % 2 == 0) { // x
                    predBBoxes.get(i)[j] *= w_ratio;
                } else { // y
                    predBBoxes.get(i)[j] *= h_ratio;
                }
            }
        }
        return predBBoxes;
    }
}
