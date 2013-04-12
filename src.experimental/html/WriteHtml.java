package html;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import antlr.collections.AST;

import com.phybots.picode.PicodeSettings;
import com.phybots.picode.ProcessingIntegration;
import com.phybots.picode.parser.ASTtoHTMLConverter;
import com.phybots.picode.parser.PdeParser;

import processing.app.PicodeSketch;
import processing.app.SketchCode;
import processing.app.SketchException;

public class WriteHtml {

	public static void main(String[] args) {
		ProcessingIntegration.init();
		new WriteHtml();
	}

	public WriteHtml() {
		PicodeSettings settings = new PicodeSettings();
		settings.load();
		PicodeSketch sketch = openSketch();
		if (sketch == null) return;
		writeHtml(sketch, settings);
	}

	private PicodeSketch openSketch() {
		FileDialog fd = new FileDialog(
				(Frame)null,
				"Open a Picode sketch...",
				FileDialog.LOAD);
		fd.setDirectory(PicodeSettings.getProjectsFolderPath());
		fd.setVisible(true);

		String newParentDir = fd.getDirectory();
	    String newName = fd.getFile();

	    // user canceled selection
	    if (newName == null) return null;

	    // check to make sure that this .pde file is
	    // in a folder of the same name
	    File parentFile = new File(newParentDir);
	    String parentName = parentFile.getName();
	    String pdeName = parentName + ".pde";
	    
	    try {
			return new PicodeSketch(null,
					parentFile.getAbsolutePath() + File.separatorChar + pdeName);
		} catch (IOException e1) {
			return null;
		} finally {
			fd.dispose();
		}
	}

	private void writeHtml(PicodeSketch sketch, PicodeSettings settings) {
		try {
			PdeParser parser = new PdeParser(sketch);
			SketchCode[] codes = sketch.getCode();
			for (int i = 0; i < codes.length; i ++) {
				SketchCode code = codes[i];
				System.out.print("Wrote: ");
				System.out.print(code.getFileName());
				System.out.println(".html");
				AST ast = parser.parse(i);
				FileWriter writer = new FileWriter(code.getFile().getAbsolutePath() + ".html");
				writer.append(
						new ASTtoHTMLConverter(parser.getPreprocessor()).convert(
								ast,
								sketch.getName(),
								settings.getHeader(),
								settings.getFooter(),
								"../../poses/"));
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SketchException se) {
			System.err.println(ProcessingIntegration.getErrorString(sketch, se));
		}
	}
}
