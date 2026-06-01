package debug;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 功能描述：日志类，文件储存与输出调试信息
 * <p>
 *     使用方式:
 *     <pre>
 *         Log.info("输出信息");
 *         Log.Debug("调试信息");
 *         Log.warn("警告操作");
 *         Log.error("错误信息");
 *     </pre>
 *     日志保存在 logs 目录下，按日期命名
 *     用 Enabled = false 关闭
 * </p>
 * @author cyt
 * @date 2026/5/30 19:45
 */
public class Log
{
    public static final boolean ENABLED = true;
    private static String currentDate = "";
    private static BufferedWriter writer = null;
    private static final String LOG_DIR = "logs";

    public static void info(String msg)
    {
        log("INFO",msg);
    }

    public static void debug(String msg)
    {
        log("DEBUG",msg);
    }

    public static void warn(String msg)
    {
        log("WARN",msg);
    }

    public static void error(String msg)
    {
        log("ERROR",msg);
    }

    /**
     * 功能描述：打印与记录信息
     * @author cyt
     * @date 2026/5/31 12:18
     */
    private static void log(String level,String msg)
    {
        if (!ENABLED)
            return;

        String line = buildLine(level,msg);

        if ("ERROR".equals(level))
            System.err.println(line);
        else
            System.out.println(line);

        writeToFile(line);
    }

    /**
     * 功能描述：创造带时间的信息
     * @author cyt
     * @date 2026/5/31 11:20
     */
    private static String buildLine(String level,String msg)
    {
        LocalDateTime now = LocalDateTime.now();
        String timeStamp = String.format("%04d-%02d-%02d %02d:%02d:%02d",now.getYear(), now.getMonthValue(), now.getDayOfMonth()
        , now.getHour(),now.getMinute(),now.getSecond());

        return "[" + timeStamp + "] [" + level + "] " + msg;
    }

    /**
     * 功能描述：创建流
     * @author cyt
     * @date 2026/5/31 12:44
     */
    private static BufferedWriter createWriter(String date) throws IOException
    {
        File dir = new File(LOG_DIR);
        // 判断是否存在
        if (!dir.exists())
        {
            boolean created = dir.mkdirs();
            if (!created)
            {
                System.err.println("无法创造目录:" + dir.getAbsolutePath());
                return null;
            }
        }

        File logFile = new File(dir,date + ".log");
        // 创建一个支持 UTF-8 编码、可以追加写入、带缓冲的高效文件写入器，并返回它
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile,true), StandardCharsets.UTF_8));
    }

    public static void close()
    {
        closeWriter();
    }

    /**
     * 功能描述：关闭流
     * @author cyt
     * @date 2026/5/31 12:43
     */
    public static void closeWriter()
    {
        if (writer != null)
        {
            try
            {
                writer.close();
            }catch (IOException ignored)
            {}

            writer = null;
        }
    }

    /**
     * 功能描述：写入文件
     * @author cyt
     * @date 2026/5/31 12:43
     */
    private static void writeToFile(String line)
    {
        try
        {
            String today = getToday();
            if (!today.equals(currentDate))
            {
                closeWriter();
                currentDate = today;
                writer = createWriter(today);
            }

            if (writer != null)
            {
                writer.write(line);
                writer.newLine();
                writer.flush();
            }
        }
        catch (IOException e)
        {
            System.err.println("写入文件失败了:" + e.getMessage());
        }

    }

    /**
     * 功能描述：获取今天日期
     * @author cyt
     * @date 2026/5/31 12:43
     */
    private static String getToday()
    {
        LocalDate today = LocalDate.now();
        return String.format("%04d-%02d-%02d",
                today.getYear(), today.getMonthValue(), today.getDayOfMonth());
    }
}
