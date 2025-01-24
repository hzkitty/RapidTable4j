package io.github.hzkitty.rapid_table.utils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

/**
 * 加载图片
 */
public class LoadImage {

    public Mat call(Object imgInput) throws LoadImageError {
        Mat mat = loadImg(imgInput);

        // 如果是单通道图像，则转换为 BGR 三通道
        if (mat.channels() == 1) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2BGR);
            return mat;
        }

        // 如果是四通道图像（RGBA），转换为 BGR
        if (mat.channels() == 4) {
            mat = cvtFourToThree(mat);
            return mat;
        }

        return mat;
    }

    /**
     * 根据传入的对象类型，加载不同来源的图像数据
     */
    private Mat loadImg(Object imgInput) throws LoadImageError {
        if (imgInput instanceof String) {
            String filePath = (String) imgInput;
            verifyExist(filePath);
            // 直接使用 OpenCV 读取 BGR
            Mat mat = Imgcodecs.imread(filePath);
            if (mat.empty()) {
                throw new LoadImageError("无法识别或读取该图片：" + filePath);
            }
            return mat;
        } else if (imgInput instanceof byte[]) {
            byte[] imgBytes = (byte[]) imgInput;
            try {
                MatOfByte mob = new MatOfByte(imgBytes);
                Mat mat = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_UNCHANGED);
                if (mat.empty()) {
                    throw new LoadImageError("无法从字节数组中解码图片");
                }
                return mat;
            } catch (Exception e) {
                throw new LoadImageError("从字节数组解码图片失败", e);
            }
        } else if (imgInput instanceof Mat) {
            return (Mat) imgInput;
        } else {
            throw new LoadImageError("不支持的图片输入类型：" + imgInput.getClass().getName());
        }
    }

    /**
     * RGBA → BGR 的转换示例
     */
    private Mat cvtFourToThree(Mat rgbaMat) {
        List<Mat> rgbaChannels = new java.util.ArrayList<>();
        Core.split(rgbaMat, rgbaChannels);

        Mat b = rgbaChannels.get(0);
        Mat g = rgbaChannels.get(1);
        Mat r = rgbaChannels.get(2);
        Mat a = rgbaChannels.get(3);

        // 合并 b、g、r
        List<Mat> bgrList = new java.util.ArrayList<>();
        bgrList.add(b);
        bgrList.add(g);
        bgrList.add(r);
        Mat bgrMat = new Mat();
        Core.merge(bgrList, bgrMat);

        // 取反 alpha 通道
        Mat notA = new Mat();
        Core.bitwise_not(a, notA);
        // 将单通道 notA 转为三通道
        Imgproc.cvtColor(notA, notA, Imgproc.COLOR_GRAY2BGR);

        // 使用 alpha 通道作为 mask 做与运算
        Mat masked = new Mat();
        Core.bitwise_and(bgrMat, bgrMat, masked, a);

        // 与取反的 alpha 通道进行加运算
        Mat result = new Mat();
        Core.add(masked, notA, result);

        return result;
    }

    /**
     * 校验文件是否存在
     */
    private void verifyExist(String filePath) throws LoadImageError {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new LoadImageError("文件不存在：" + filePath);
        }
    }
}