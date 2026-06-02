package shared;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：WebSocket 消息封装，包含类型和可变数据字段
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class Message
{
    private MessageType type;
    private final Map<String, Object> data;

    public Message(MessageType type)
    {
        this.type = type;
        this.data = new HashMap<>();
    }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public Message put(String key, Object value)
    {
        data.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) { return (T) data.get(key); }

    public <T> T get(String key, T defaultValue)
    {
        T val = get(key);
        return val != null ? val : defaultValue;
    }

    public boolean has(String key) { return data.containsKey(key); }

    public Map<String, Object> getData() { return data; }
}
