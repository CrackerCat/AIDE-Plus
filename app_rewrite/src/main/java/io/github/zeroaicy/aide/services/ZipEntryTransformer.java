package io.github.zeroaicy.aide.services;

import java.util.zip.ZipEntry;
import java.util.Set;

public interface ZipEntryTransformer {
	/**
	 * 返回null，表示过滤掉
	 */
	public ZipEntry transformer(ZipEntry zipEntry, PackagingStream packagingStream);

	/**
	 * 处理libgdx库
	 */
	public class LibgdxNativesTransformer extends FilterTransformer implements ZipEntryTransformer {

		private String curLibgdxNativesABI;
		public void setCurLibgdxNativesLibsPath(String path) {
			if (path.endsWith("natives-arm64-v8a.jar")) {
				curLibgdxNativesABI = "arm64-v8a";
			} else if (path.endsWith("natives-armeabi-v7a.jar")) {
				curLibgdxNativesABI = "armeabi-v7a";
			} else if (path.endsWith("natives-armeabi.jar")) {
				curLibgdxNativesABI = "armeabi";
			} else if (path.endsWith("natives-x86_64.jar")) {
				curLibgdxNativesABI = "x86_64";
			} else if (path.endsWith("natives-x86.jar")) {
				curLibgdxNativesABI = "x86";
			} else {
				curLibgdxNativesABI = null;
			}
		}

		@Override
		public ZipEntry transformer(ZipEntry zipEntry, PackagingStream packagingStream) {

			// 过滤文件夹
			if (zipEntry.isDirectory()) {
				return null;
			}
			if (curLibgdxNativesABI == null) {
				return null;
			}

			String zipEntryName = zipEntry.getName();

			// 允许 debug模式 非so文件打包
			if (!this.isAndroidDebuggable() && !zipEntryName.endsWith(".so")) {
				return null;
			}

			// 计算 libgdx native entry名称
			zipEntryName = "lib/" + this.curLibgdxNativesABI + "/" + zipEntryName;

			// 检查是否已添加
			if (this.isAdded(packagingStream, zipEntryName)) {
				return null;
			}

			// 检查是否已过滤 abi
			if (this.isFilterAbi(zipEntryName)) {
				return null;
			}

			ZipEntry newZipEntry = new ZipEntry(zipEntryName);
			if (!this.isAndroidExtractNativeLibs()) {
				//android:extractNativeLibs="false"时必须无压缩
				newZipEntry.setMethod(ZipEntry.STORED);
			}
			return newZipEntry;
		}
	}
	/**
	 * dex.zip转换器
	 * 不过滤任何资源，即都会添加
	 * 但是会重命名classes%d.dex式文件
	 */
	public class DexZipTransformer extends ZipResourceTransformer implements ZipEntryTransformer {
		protected int classesCountDex = 1;
		@Override
		public ZipEntry transformer(ZipEntry zipEntry, PackagingStream packagingStream) {
			String zipEntryFileName = zipEntry.getName();
			//因为classes.dex会重名命，所以必须先判断
			if (isNotClassesDex(zipEntry, zipEntryFileName)) {
				// 交给 ZipResourceTransformer处理
				return super.transformer(zipEntry, packagingStream);
			}

			// 处理 "classes%d.dex"
			String dexEntryName = this.classesCountDex > 1
					? String.format("classes%d.dex", this.classesCountDex)
					: "classes.dex";

			//查询 dexEntryName是否已添加
			while (this.isAdded(packagingStream, dexEntryName)
					// classesCountDex 不可能大于已添加的文件数
					&& this.classesCountDex < packagingStream.getZipEntryCount() + 1) {
				this.classesCountDex++;
				dexEntryName = String.format("classes%d.dex", this.classesCountDex);
			}
			return new ZipEntry(dexEntryName);
		}

		/**
		 * 不是ClassesDex文件
		 */
		private boolean isNotClassesDex(ZipEntry zipEntry, String zipEntryFileName) {
			zipEntryFileName = zipEntryFileName.toLowerCase();
			boolean endsWith = zipEntry.isDirectory() || zipEntryFileName.contains("/")
					|| !zipEntryFileName.startsWith("classes") || !zipEntryFileName.endsWith(".dex");
			return endsWith;
		}
	}

	/**
	 * zip资源转换器 所有一般资源转换器的父类
	 * 因为是从jar库添加资源，所以 class与java文件都不能添加
	 */
	public class ZipResourceTransformer extends FilterTransformer implements ZipEntryTransformer {
		@Override
		public ZipEntry transformer(ZipEntry zipEntry, PackagingStream packagingStream) {
			String zipEntryName = zipEntry.getName();
			//过滤已存在的
			if (this.isAdded(packagingStream, zipEntryName)) {
				return null;
			}

			// 处理 assets资源目录(因为AIDE+的自举，所以不过滤 assets/ 下的 .class .java 文件)
			if (zipEntryName.startsWith("assets/")) {
				ZipEntry newZipEntry = new ZipEntry(zipEntryName);
				// assets/下资源必须无压缩
				newZipEntry.setMethod(ZipEntry.STORED);
				return newZipEntry;
			}

			// 处理 lib 下 so文件
			if (zipEntryName.startsWith("lib/")) {
				// 检查 abi 是否过滤
				if (this.isFilterAbi(zipEntryName)) {
					return null;
				}

				// 处理 非so文件 非debug 不打包
				if (zipEntryName.endsWith(".so")) {
					if (this.isAndroidExtractNativeLibs()) {
						return zipEntry;
					}
					//  android:extractNativeLibs false 
					// lib/abi/xxx.so 下资源必须无压缩
					ZipEntry newZipEntry = new ZipEntry(zipEntryName);
					newZipEntry.setMethod(ZipEntry.STORED);
					return newZipEntry;

				}
				// 此时 非so文件
				
				// 允许 debug模式 非so文件打包
				if (!this.isAndroidDebuggable()) {
					return null;
				}
			}

			String zipEntryNameLowerCase = zipEntryName.toLowerCase();
			// 过滤 class文件
			if (zipEntryNameLowerCase.endsWith(".class") 
				// 过滤 java文件
				|| zipEntryNameLowerCase.endsWith(".java")) {
				return null;
			}

			return zipEntry;
		}
	}

	/**
	 * 从文件夹添加的so转换器
	 */
	public class NativeLibFileTransformer extends FilterTransformer implements ZipEntryTransformer {
		@Override
		public ZipEntry transformer(ZipEntry zipEntry, PackagingStream packagingStream) {
			String zipEntryName = zipEntry.getName();

			String[] split = zipEntryName.split("/");
			if (split.length >= 2) {
				//只取so父目录/so文件名
				zipEntryName = "lib/" + split[split.length - 2] + "/" + split[split.length - 1];
			}

			// 允许 debug模式 非so文件打包
			if (!zipEntryName.endsWith(".so") && !this.isAndroidDebuggable()) {
				return null;
			}
			//以包含过滤
			// 检查是否已添加
			if (this.isAdded(packagingStream, zipEntryName)) {
				return null;
			}
			// 检查是否已过滤 abi
			if (this.isFilterAbi(zipEntryName)) {
				return null;
			}
			ZipEntry newZipEntry = new ZipEntry(zipEntryName);

			if (!this.isAndroidExtractNativeLibs()) {
				//android:extractNativeLibs="false"时必须无压缩
				newZipEntry.setMethod(ZipEntry.STORED);
			}
			return newZipEntry;
		}
	}

	public static abstract class FilterTransformer implements ZipEntryTransformer {
		private Set<String> abiFilters;

		public void setAbiFilters(Set<String> abiFilters) {
			this.abiFilters = abiFilters;
		}

		public boolean isFilterAbi(ZipEntry zipEntry) {
			if (this.abiFilters == null) {
				// 没有 abiFilters 就必须都打包
				return false;
			}
			if (zipEntry == null) {
				return true;
			}
			return isFilterAbi(zipEntry.getName());
		}

		public boolean isFilterAbi(String zipEntryName) {
			if (this.abiFilters == null || this.abiFilters.isEmpty()) {
				// 没有 abiFilters 就必须都打包
				return false;
			}

			if (zipEntryName == null) {
				return true;
			}

			int abiEndIndex;
			if (!zipEntryName.startsWith("lib/") || (abiEndIndex = zipEntryName.indexOf('/', "lib/".length())) < 0) {
				return true;
			}

			String abi = zipEntryName.substring("lib/".length(), abiEndIndex);
			// 没有就过滤
			return !this.abiFilters.contains(abi);
		}

		public boolean isAdded(PackagingStream packagingStream, String zipEntryName) {
			return packagingStream.contains(zipEntryName);
		}

		private boolean androidExtractNativeLibs;
		private boolean androidDebuggable;

		public void setAndroidExtractNativeLibs(boolean androidExtractNativeLibs) {
			this.androidExtractNativeLibs = androidExtractNativeLibs;
		}

		public boolean isAndroidExtractNativeLibs() {
			return androidExtractNativeLibs;
		}

		public void setAndroidDebuggable(boolean androidDebuggable) {
			this.androidDebuggable = androidDebuggable;
		}

		public boolean isAndroidDebuggable() {
			return androidDebuggable;
		}
	}
}

