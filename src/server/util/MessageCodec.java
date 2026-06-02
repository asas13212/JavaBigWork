package server.util;

import shared.Message;
import shared.MessageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述：手动 JSON 编解码器，零外部依赖，用于 WebSocket 消息的序列化与反序列化
 * @author cyt & Claude
 * @date 2026/6/2
 */
public class MessageCodec
{
    /**
     * 将 Message 对象编码为 JSON 字符串
     * 格式: {"type":"ROLL_DICE","data":{"roomId":4821}}
     */
    public static String encode(Message message)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"");
        sb.append(message.getType().name());
        sb.append("\",\"data\":");
        encodeValue(message.getData(), sb);
        sb.append("}");
        return sb.toString();
    }

    /**
     * 将 JSON 字符串解码为 Message 对象
     */
    public static Message decode(String json)
    {
        Parser parser = new Parser(json.trim());
        Map<String, Object> root = parser.parseObject();

        String typeName = (String) root.get("type");
        if (typeName == null)
        {
            throw new IllegalArgumentException("JSON missing 'type' field");
        }

        MessageType type;
        try
        {
            type = MessageType.valueOf(typeName);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Unknown MessageType: " + typeName, e);
        }

        Message message = new Message(type);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) root.get("data");
        if (data != null)
        {
            for (Map.Entry<String, Object> entry : data.entrySet())
            {
                message.put(entry.getKey(), entry.getValue());
            }
        }

        return message;
    }

    // ---- encoding helpers ----

    @SuppressWarnings("unchecked")
    private static void encodeValue(Object value, StringBuilder sb)
    {
        if (value == null)
        {
            sb.append("null");
        }
        else if (value instanceof String s)
        {
            sb.append("\"");
            escapeString(s, sb);
            sb.append("\"");
        }
        else if (value instanceof Integer i)
        {
            sb.append(i);
        }
        else if (value instanceof Long l)
        {
            sb.append(l);
        }
        else if (value instanceof Double d)
        {
            sb.append(d);
        }
        else if (value instanceof Boolean b)
        {
            sb.append(b);
        }
        else if (value instanceof Map<?, ?> m)
        {
            encodeMap((Map<String, Object>) m, sb);
        }
        else if (value instanceof List<?> list)
        {
            encodeList(list, sb);
        }
        else
        {
            // fallback: treat toString as string
            sb.append("\"");
            escapeString(value.toString(), sb);
            sb.append("\"");
        }
    }

    private static void encodeMap(Map<String, Object> map, StringBuilder sb)
    {
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"");
            escapeString(entry.getKey(), sb);
            sb.append("\":");
            encodeValue(entry.getValue(), sb);
        }
        sb.append("}");
    }

    private static void encodeList(List<?> list, StringBuilder sb)
    {
        sb.append("[");
        boolean first = true;
        for (Object item : list)
        {
            if (!first) sb.append(",");
            first = false;
            encodeValue(item, sb);
        }
        sb.append("]");
    }

    private static void escapeString(String s, StringBuilder sb)
    {
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            switch (c)
            {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default ->
                {
                    if (c < 0x20)
                    {
                        sb.append(String.format("\\u%04x", (int) c));
                    }
                    else
                    {
                        sb.append(c);
                    }
                }
            }
        }
    }

    // ---- decoding parser ----

    private static class Parser
    {
        private final String json;
        private int pos;

        Parser(String json)
        {
            this.json = json;
            this.pos = 0;
        }

        Map<String, Object> parseObject()
        {
            skipWhitespace();
            expect('{');
            skipWhitespace();
            Map<String, Object> map = new HashMap<>();
            if (peek() == '}')
            {
                pos++;
                return map;
            }
            while (true)
            {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                char c = peek();
                if (c == '}')
                {
                    pos++;
                    return map;
                }
                else if (c == ',')
                {
                    pos++;
                }
                else
                {
                    throw new IllegalArgumentException("Expected ',' or '}' at position " + pos + ", got '" + c + "'");
                }
            }
        }

        List<Object> parseArray()
        {
            skipWhitespace();
            expect('[');
            skipWhitespace();
            List<Object> list = new ArrayList<>();
            if (peek() == ']')
            {
                pos++;
                return list;
            }
            while (true)
            {
                skipWhitespace();
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                char c = peek();
                if (c == ']')
                {
                    pos++;
                    return list;
                }
                else if (c == ',')
                {
                    pos++;
                }
                else
                {
                    throw new IllegalArgumentException("Expected ',' or ']' at position " + pos + ", got '" + c + "'");
                }
            }
        }

        Object parseValue()
        {
            skipWhitespace();
            char c = peek();
            return switch (c)
            {
                case '"' -> parseString();
                case '{' -> parseObject();
                case '[' -> parseArray();
                case 't', 'f' -> parseBoolean();
                case 'n' -> parseNull();
                default ->
                {
                    if (c == '-' || (c >= '0' && c <= '9'))
                    {
                        yield parseNumber();
                    }
                    throw new IllegalArgumentException("Unexpected character at position " + pos + ": '" + c + "'");
                }
            };
        }

        String parseString()
        {
            skipWhitespace();
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < json.length())
            {
                char c = json.charAt(pos);
                if (c == '"')
                {
                    pos++;
                    return sb.toString();
                }
                else if (c == '\\')
                {
                    pos++;
                    if (pos >= json.length())
                    {
                        throw new IllegalArgumentException("Unexpected end of JSON in string escape");
                    }
                    char escaped = json.charAt(pos);
                    switch (escaped)
                    {
                        case '"'  -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/'  -> sb.append('/');
                        case 'b'  -> sb.append('\b');
                        case 'f'  -> sb.append('\f');
                        case 'n'  -> sb.append('\n');
                        case 'r'  -> sb.append('\r');
                        case 't'  -> sb.append('\t');
                        case 'u'  ->
                        {
                            if (pos + 4 >= json.length())
                            {
                                throw new IllegalArgumentException("Unexpected end of JSON in unicode escape");
                            }
                            String hex = json.substring(pos + 1, pos + 5);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        }
                        default -> throw new IllegalArgumentException("Invalid escape character: \\" + escaped);
                    }
                    pos++;
                }
                else
                {
                    sb.append(c);
                    pos++;
                }
            }
            throw new IllegalArgumentException("Unterminated string");
        }

        Object parseNumber()
        {
            skipWhitespace();
            int start = pos;
            if (peek() == '-') pos++;
            while (pos < json.length() && json.charAt(pos) >= '0' && json.charAt(pos) <= '9')
            {
                pos++;
            }
            // check for floating point
            if (pos < json.length() && json.charAt(pos) == '.')
            {
                pos++;
                while (pos < json.length() && json.charAt(pos) >= '0' && json.charAt(pos) <= '9')
                {
                    pos++;
                }
                double value = Double.parseDouble(json.substring(start, pos));
                // if it fits in an integer without loss, return Integer
                if (value == Math.floor(value) && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE)
                {
                    return (int) value;
                }
                return value;
            }
            String numStr = json.substring(start, pos);
            try
            {
                return Integer.parseInt(numStr);
            }
            catch (NumberFormatException e)
            {
                return Long.parseLong(numStr);
            }
        }

        Boolean parseBoolean()
        {
            skipWhitespace();
            if (json.startsWith("true", pos))
            {
                pos += 4;
                return true;
            }
            else if (json.startsWith("false", pos))
            {
                pos += 5;
                return false;
            }
            throw new IllegalArgumentException("Expected boolean at position " + pos);
        }

        Object parseNull()
        {
            skipWhitespace();
            if (json.startsWith("null", pos))
            {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Expected null at position " + pos);
        }

        void expect(char expected)
        {
            if (pos >= json.length())
            {
                throw new IllegalArgumentException("Unexpected end of JSON, expected '" + expected + "'");
            }
            char c = json.charAt(pos);
            if (c != expected)
            {
                throw new IllegalArgumentException("Expected '" + expected + "' at position " + pos + ", got '" + c + "'");
            }
            pos++;
        }

        char peek()
        {
            skipWhitespace();
            if (pos >= json.length())
            {
                throw new IllegalArgumentException("Unexpected end of JSON");
            }
            return json.charAt(pos);
        }

        void skipWhitespace()
        {
            while (pos < json.length())
            {
                char c = json.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r')
                {
                    pos++;
                }
                else
                {
                    break;
                }
            }
        }
    }
}
