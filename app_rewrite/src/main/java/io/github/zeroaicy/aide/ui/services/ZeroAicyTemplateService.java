/**
 * @Date 
 * @AIDE AIDE+ 
 */
package io.github.zeroaicy.aide.ui.services;

import com.aide.common.AppLog;
import com.aide.common.StreamUtilities;
import com.aide.ui.ServiceContainer;
import com.aide.ui.services.TemplateService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZeroAicyTemplateService extends TemplateService {

	@Override
	public List<String> j6(String dir, String projectName, String string2, String package_name,
			TemplateService.TemplateGroup templateGroup) {

		return super.j6(dir, projectName, string2, package_name, templateGroup);
	}

	public List<String> j6_2(String str, String str2, String str3, String str4, TemplateGroup templateGroup) {

		String replace = str3.replace('.', '_');
		ArrayList arrayList = new ArrayList();
		try {
			String Hw = Hw(str, str2);
			new File(Hw).mkdirs();
			InputStream open = ServiceContainer.getContext().getAssets()
					.open("templates/" + templateGroup.templateResourceName);
			Map<String, String> FH = FH(open, true, Hw, str2, str3, replace, str4, templateGroup.FH);
			open.close();
			for (String str5 : templateGroup.FH) {
				if (FH.containsKey(str5)) {
					arrayList.add(FH.get(str5));
				}
			}
			ServiceContainer.getDropboxService().ef(Hw);
		} catch (IOException e) {
			AppLog.e(e);
		}
		return arrayList;
	}

	private Map<String, String> FH(InputStream inputStream, boolean z, String createDir, String project_name, String str3,
			String str4, String str5, String[] strArr) throws IOException {
		String str6 = str5;
		HashMap<String, String> hashMap = new HashMap<String, String>();
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		while (true) {
			ZipEntry nextEntry = zipInputStream.getNextEntry();
			if (nextEntry == null) {
				return hashMap;
			}
			String extractFileName = nextEntry.getName()
					// project_name
					.replace("$package_name$", str3.replace('.', File.separatorChar))
					// 
					.replace("$project_name$", project_name)
					//
					.replace("$flavor_name$", str6);
					
			String extractFilePath = createDir + File.separator + extractFileName;
			
			AppLog.d("Extract " + extractFileName + " to " + extractFilePath);
			
			try {
				if (nextEntry.isDirectory()) {
					new File(extractFilePath).mkdirs();
				} else {
					int length = strArr.length;
					int i = 0;
					while (i < length) {
						String str8 = strArr[i];
						StringBuilder sb = new StringBuilder();
						int i2 = length;
						sb.append("/");
						sb.append(str8);
						if (extractFilePath.endsWith(sb.toString())) {
							hashMap.put(str8, extractFilePath);
						}
						i++;
						length = i2;
					}
					if (z || !new File(extractFilePath).isFile()) {
						new File(extractFilePath).getParentFile().mkdirs();
						String lowerCase = extractFilePath.toLowerCase(Locale.ENGLISH);
						if (!lowerCase.endsWith("build.gradle") && !lowerCase.endsWith(".java")
								&& !lowerCase.endsWith(".xml") && !lowerCase.endsWith(".c")
								&& !lowerCase.endsWith(".cpp") && !lowerCase.endsWith(".cc")
								&& !lowerCase.endsWith(".h") && !lowerCase.endsWith(".hpp")
								&& !lowerCase.endsWith(".html") && !lowerCase.endsWith(".htm")
								&& !lowerCase.endsWith(".css") && !lowerCase.endsWith(".js")) {
							FileOutputStream fileOutputStream = new FileOutputStream(extractFilePath);
							StreamUtilities.transferStream(zipInputStream, fileOutputStream, false);
							fileOutputStream.close();
						}
						String replace2 = StreamUtilities
								.readTextReader(new BufferedReader(new InputStreamReader(zipInputStream)))
								.replace("\r\n", "\n").replace("$project_name$", project_name)
								.replace("$package_name$", str3).replace("$package_name_jni$", str4);
						OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(extractFilePath));
						outputStreamWriter.write(replace2);
						outputStreamWriter.close();
					}
				}
			} catch (IOException e) {
				AppLog.e(e);
			}
			str6 = str5;
		}
	}
}

