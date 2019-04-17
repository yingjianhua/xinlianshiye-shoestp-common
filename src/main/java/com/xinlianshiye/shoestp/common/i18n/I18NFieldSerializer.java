package com.xinlianshiye.shoestp.common.i18n;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import lombok.extern.slf4j.Slf4j;

/**
 *
 *
 * <h5>国际化字段的JSON序列化器</h5>
 *
 * <li>在需要做国际化处理的字段上添加
 * <li>{@code @JsonSerialize(using=I18NFieldSerializer.class)}
 * <li>{@link I18NFieldSerializer}
 *
 * @author yingjianhua
 */
@Slf4j
public class I18NFieldSerializer extends JsonSerializer<String> {

  @Autowired private WebApplicationContext context;

  public static final String SESSION_LANG = "session_lang";

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    HttpSession session = (HttpSession) context.getServletContext().getAttribute("session");
    do {
      Locale lang;
      try {
        lang = (Locale) session.getAttribute(SESSION_LANG);
      } catch (Exception e) {
        gen.writeString(value);
        break;
      }
      if (value == null || "".equals(value.trim())) {
        gen.writeNull();
        break;
      }
      String content;
      try {

        JsonNode jsonNode = new ObjectMapper().readTree(value);
        content = jsonNode.path(lang.getDisplayName()).asText();
        if ("".equals(content)) {
          content = jsonNode.path(Locale.ENGLISH.getDisplayName()).asText();
          if ("".equals(content)) {
            gen.writeNull();
            break;
          }
        }
        gen.writeString(content);
        break;
      } catch (JsonParseException e) {
        log.debug(value + ":不是json格式!");
        gen.writeString(value);
        break;
      }
    } while (true);
  }
}
