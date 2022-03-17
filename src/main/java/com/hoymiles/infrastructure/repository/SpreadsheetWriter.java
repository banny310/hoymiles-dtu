package com.hoymiles.infrastructure.repository;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.hoymiles.infrastructure.dtu.utils.DateUtil;
import io.netty.util.CharsetUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;

import jakarta.enterprise.context.Dependent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Dependent
@Log4j2
public class SpreadsheetWriter {

    private static final String PATH = "./data/sheet/msg_%s.xlsx";

    public void write(int code, Message msg) {
        try {
            String filePath = String.format(PATH, code);

            XSSFWorkbook workbook = createOrOpenSheet(filePath);

            Descriptors.Descriptor descriptor = msg.getDescriptorForType();

            write(msg, descriptor, workbook);

            File file = new File(filePath);
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private @NotNull XSSFWorkbook createOrOpenSheet(String filePath) throws IOException {
        File file = new File(filePath);
        File dir = new File(file.getAbsoluteFile().getParent());
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create dirs " + filePath);
        }
        if (file.exists()) {
            if (!file.canRead()) {
                throw new IOException("Cannot read file " + filePath);
            }
            return new XSSFWorkbook(new FileInputStream(file));
        }

        return new XSSFWorkbook();
    }

    private void write(Message msg, Descriptors.Descriptor descriptor, @NotNull XSSFWorkbook workbook) {
        Descriptors.FieldDescriptor fdMin = getLowestField(descriptor);
        String sheetName = fdMin.getContainingType().getName();
        Sheet sheet = workbook.getSheet(sheetName);

        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }
        if (sheet.getLastRowNum() == -1) {
            writeHeader(descriptor, sheet.createRow(0));
        }

        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        String datetime = new SimpleDateFormat(DateUtil.DATE_FORMAT).format(new Date(System.currentTimeMillis()));
        row.createCell(0, CellType.STRING).setCellValue(datetime);
        row.createCell(1, CellType.STRING).setCellValue(msg.getUnknownFields().toString());

        int offset = 2;
        descriptor.getFields().forEach(fd -> {
            Object o = msg.getField(fd);
            switch (fd.getJavaType()) {
                case INT:
                    row.createCell(fd.getIndex() + offset, CellType.NUMERIC)
                            .setCellValue(((int) o));
                    break;
                case LONG:
                    row.createCell(fd.getIndex() + offset, CellType.NUMERIC)
                            .setCellValue((long) o);
                    break;
                case BYTE_STRING:
                    row.createCell(fd.getIndex() + offset, CellType.STRING)
                            .setCellValue(((ByteString) o).toString(CharsetUtil.ISO_8859_1));
                    break;
                case MESSAGE:
                    if (fd.isRepeated()) {
                        ((List<?>) o).forEach(el -> write((Message) el, fd.getMessageType(), workbook));
                    } else {
                        write((Message) o, fd.getMessageType(), workbook);
                    }
                    break;
            }
        });
    }

    private void writeHeader(Descriptors.@NotNull Descriptor descriptor, @NotNull Row row) {
        row.createCell(0, CellType.STRING).setCellValue("datetime");
        row.createCell(1, CellType.STRING).setCellValue("unknown");

        descriptor.getFields().forEach((fd) -> {
            switch (fd.getJavaType()) {
                case INT:
                case LONG:
                case BYTE_STRING:
                    row.createCell(fd.getIndex() + 2, CellType.STRING).setCellValue(fd.getJsonName());
                    break;
                default:
                    break;
            }
        });
    }

    private static Descriptors.FieldDescriptor getLowestField(Descriptors.@NotNull Descriptor descriptor) throws NoSuchElementException {
        Integer idx = descriptor.getFields().stream()
                .map(Descriptors.FieldDescriptor::getIndex)
                .sorted()
                .findFirst().orElseThrow();

        return descriptor.getFields().stream()
                .filter(fd -> fd.getIndex() == idx)
                .findFirst().orElse(null);
    }
}
