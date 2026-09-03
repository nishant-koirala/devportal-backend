package com.fonepay.devportal.common.config;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fonepay.devportal.modules.cms.document.Block;
import com.fonepay.devportal.modules.cms.document.BlockData;
import com.fonepay.devportal.modules.cms.document.CodeBlockData;
import com.fonepay.devportal.modules.cms.document.EndpointBlockData;
import com.fonepay.devportal.modules.cms.document.FaqBlockData;
import com.fonepay.devportal.modules.cms.document.HeadingBlockData;
import com.fonepay.devportal.modules.cms.document.ImageBlockData;
import com.fonepay.devportal.modules.cms.document.NoteWarningBlockData;
import com.fonepay.devportal.modules.cms.document.ParagraphBlockData;
import com.fonepay.devportal.modules.cms.document.ParameterTableBlockData;
import com.fonepay.devportal.modules.cms.document.TableBlockData;
import com.fonepay.devportal.modules.cms.document.TestCredentialBlockData;
import com.fonepay.devportal.modules.cms.enums.BlockType;

/**
 * Reads/writes {@link Block} using the stored {@code type} field so Mongo does not
 * need {@code _class} to instantiate {@link BlockData}.
 */
public final class BlockMongoConverters {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private BlockMongoConverters() {
    }

    public static List<Converter<?, ?>> all() {
        return List.of(new ReadConverter(), new WriteConverter());
    }

    @ReadingConverter
    public static class ReadConverter implements Converter<Document, Block> {
        @Override
        public Block convert(Document source) {
            Block block = new Block();
            block.setId(source.getString("id"));
            BlockType type = parseType(source.get("type"));
            block.setType(type);

            Integer order = source.getInteger("order");
            block.setOrder(order != null ? order : 0);

            Object data = source.get("data");
            if (data != null && type != null) {
                block.setData(MAPPER.convertValue(data, classFor(type)));
            }
            return block;
        }
    }

    @WritingConverter
    public static class WriteConverter implements Converter<Block, Document> {
        @Override
        public Document convert(Block source) {
            Document document = new Document();
            document.put("id", source.getId());
            if (source.getType() != null) {
                document.put("type", source.getType().name());
            }
            document.put("order", source.getOrder());
            if (source.getData() != null) {
                Map<String, Object> data = MAPPER.convertValue(source.getData(), new TypeReference<>() {
                });
                document.put("data", new Document(data));
            }
            return document;
        }
    }

    private static BlockType parseType(Object rawType) {
        if (rawType == null) {
            return null;
        }
        if (rawType instanceof BlockType blockType) {
            return blockType;
        }
        String name = rawType.toString().trim();
        if (name.isEmpty()) {
            return null;
        }
        return BlockType.valueOf(name);
    }

    private static Class<? extends BlockData> classFor(BlockType type) {
        return switch (type) {
            case HEADING -> HeadingBlockData.class;
            case PARAGRAPH -> ParagraphBlockData.class;
            case CODE -> CodeBlockData.class;
            case ENDPOINT -> EndpointBlockData.class;
            case FAQ -> FaqBlockData.class;
            case TABLE -> TableBlockData.class;
            case IMAGE -> ImageBlockData.class;
            case NOTE_WARNING -> NoteWarningBlockData.class;
            case PARAMETER_TABLE -> ParameterTableBlockData.class;
            case TEST_CREDENTIAL -> TestCredentialBlockData.class;
        };
    }
}
