package io.github.hzkitty.rapid_table;

import io.github.hzkitty.entity.*;
import io.github.hzkitty.rapid_table.entity.TableResult;
import io.github.hzkitty.rapid_table.table_matcher.TableMatch;
import io.github.hzkitty.rapid_table.table_structure.TableStructurer;
import io.github.hzkitty.rapid_table.utils.LoadImage;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RapidTable {

    private String modelType;
    private LoadImage loadImg;
    private TableStructurer tableStructure;
    private TableMatch tableMatcher;

    public RapidTable() {
        this("models/slanet-plus.onnx", "slanet-plus", false);
    }

    public RapidTable(String modelPath, String modelType, boolean useCuda) {
        this.modelType = modelType;
        // 初始化 LoadImage
        this.loadImg = new LoadImage();

        OrtInferConfig config = new OrtInferConfig();
        config.setModelPath(modelPath);
        config.setUseCuda(useCuda);

        // 1. 初始化表格结构识别器
        this.tableStructure = new TableStructurer(config);

        // 2. 初始化表格匹配器
        this.tableMatcher = new TableMatch();
    }


    public TableResult run(Object imgContent, List<RecResult> ocrResult, boolean returnLogicPoints) throws Exception {
        // 1. 加载图像
        Mat img = this.loadImg.call(imgContent);

        long startTime = System.currentTimeMillis();
        int h = img.rows();
        int w = img.cols();

        // 3. 解析 dt_boxes, rec_res
        Pair<List<float[]>, List<Pair<String, Float>>> boxAndRec = getBoxesRecs(ocrResult, h, w);
        List<float[]> dtBoxes = boxAndRec.getLeft();
        List<Pair<String, Float>> recRes = boxAndRec.getRight();

        // 4. 表格结构推理: pred_structures, pred_bboxes, ...
        Triple<List<String>, List<float[]>, Double> structureRes = this.tableStructure.call(img);
        List<String> predStructures = structureRes.getLeft();
        List<float[]> predBBoxes = structureRes.getMiddle();

        // 如果是 slanet-plus，需要缩放
        if ("slanet-plus".equalsIgnoreCase(this.modelType)) {
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
