import org.apache.commons.io.FileUtils
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files


fun replaceIdsInDocument(inputs: List<Pair<String,String>>, input: File, output: FileOutputStream) {
    try {
        /**
         * if uploaded doc then use HWPF else if uploaded Docx file use
         * XWPFDocument
         */
        // Create folders and copy documents
        if (!mainFolder.toFile().exists()) Files.createDirectories(mainFolder)
        if (!template.exists()) {
            getResource("template.docx")?.let {
                FileUtils.copyInputStreamToFile(it, template);
            }
        }
        val doc = XWPFDocument(
            OPCPackage.open(input)
        )
        for (p in doc.paragraphs) {
            val runs = p.runs
            if (runs != null) {
                for (r in runs) {
                    var text = r.getText(0)
                    inputs.forEach { input ->
                        val key = input.first
                        if (text != null && text.contains(key)) {
                            text = text.replace(key, input.second)
                            r.setText(text, 0)
                        }
                    }
                }
            }
        }
        for (tbl in doc.tables) {
            for (row in tbl.rows) {
                for (cell in row.tableCells) {
                    for (p in cell.paragraphs) {
                        for (r in p.runs) {
                            var text = r.getText(0)
                            inputs.forEach { input ->
                                val key = input.first
                                if (text != null && text.contains(key)) {
                                    text = text.replace(key, input.second)
                                    r.setText(text, 0)
                                }
                            }
                        }
                    }
                }
            }
        }
        doc.write(output)
        output.close()
    } finally {
    }
}