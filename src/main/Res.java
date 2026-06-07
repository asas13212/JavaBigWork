package main;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * 功能描述：资源加载工具类，从 classpath 加载图片资源，确保打包后也能正常访问
 * @author cyt & Claude
 * @date 2026/6/7
 */
public class Res
{
    private static final String IMG_PREFIX = "/img/";

    /**
     * 功能描述：从 classpath 加载图标
     * @param path 相对于 /img/ 的路径，如 "gameMenu/1.png"
     * @return ImageIcon 对象，找不到则返回空图标
     * @author cyt & Claude
     * @date 2026/6/7
     */
    public static ImageIcon icon(String path)
    {
        URL url = Res.class.getResource(IMG_PREFIX + path);
        if (url == null)
        {
            System.err.println("[Res] Resource not found: " + IMG_PREFIX + path);
            return new ImageIcon();
        }
        return new ImageIcon(url);
    }

    /**
     * 功能描述：从 classpath 加载图片
     * @param path 相对于 /img/ 的路径，如 "gameMenu/1.png"
     * @return Image 对象，找不到则返回空图片
     * @author cyt & Claude
     * @date 2026/6/7
     */
    public static Image image(String path)
    {
        return icon(path).getImage();
    }

    /**
     * 功能描述：从 classpath 获取资源 URL
     * @param path 相对于 /img/ 的路径，如 "leaderPic/1.png"
     * @return URL 对象，找不到则返回 null
     * @author cyt & Claude
     * @date 2026/6/7
     */
    public static URL url(String path)
    {
        URL url = Res.class.getResource(IMG_PREFIX + path);
        if (url == null)
        {
            System.err.println("[Res] Resource not found: " + IMG_PREFIX + path);
        }
        return url;
    }
}
