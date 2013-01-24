package com.pku.yangliu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/** 
 * Newsgroups文档集预处理类
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 */
public class DataPreProcess {
	
	/**输入文件调用处理数据函数
	 * @param strDir newsgroup文件目录的绝对路径
	 * @throws IOException 
	 */
	public void doProcess(String strDir) throws IOException{
		File fileDir = new File(strDir);
		if(!fileDir.exists()){
			System.out.println("File not exist:" + strDir);
			return;
		}
		String subStrDir = strDir.substring(strDir.lastIndexOf('/'));
		String dirTarget = strDir + "/../../processedSample_includeNotSpecial"+subStrDir;
		File fileTarget = new File(dirTarget);
		if(!fileTarget.exists()){//注意processedSample需要先建立目录建出来，否则会报错，因为母目录不存在
			fileTarget.mkdir();
		}
		File[] srcFiles = fileDir.listFiles();
		String[] stemFileNames = new String[srcFiles.length];
		for(int i = 0; i < srcFiles.length; i++){
			String fileFullName = srcFiles[i].getCanonicalPath();
			String fileShortName = srcFiles[i].getName();
			if(!new File(fileFullName).isDirectory()){//确认子文件名不是目录如果是可以再次递归调用
				System.out.println("Begin preprocess:"+fileFullName);
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(dirTarget + "/" + fileShortName);
				createProcessFile(fileFullName, stringBuilder.toString());
				stemFileNames[i] = stringBuilder.toString();
			}
			else {
				fileFullName = fileFullName.replace("\\","/");
				doProcess(fileFullName);
			}
		}
		//下面调用stem算法
		if(stemFileNames.length > 0 && stemFileNames[0] != null){
			Stemmer.porterMain(stemFileNames);
		}
	}
	
	/**进行文本预处理生成目标文件
	 * @param srcDir 源文件文件目录的绝对路径
	 * @param targetDir 生成的目标文件的绝对路径
	 * @throws IOException 
	 */
	private static void createProcessFile(String srcDir, String targetDir) throws IOException {
		// TODO Auto-generated method stub
		FileReader srcFileReader = new FileReader(srcDir);
		FileReader stopWordsReader = new FileReader("F:/DataMiningSample/stopwords.txt");
		FileWriter targetFileWriter = new FileWriter(targetDir);	
		BufferedReader srcFileBR = new BufferedReader(srcFileReader);//装饰模式
		BufferedReader stopWordsBR = new BufferedReader(stopWordsReader);
		String line, resLine, stopWordsLine;
		//用stopWordsBR够着停用词的ArrayList容器
		ArrayList<String> stopWordsArray = new ArrayList<String>();
		while((stopWordsLine = stopWordsBR.readLine()) != null){
			if(!stopWordsLine.isEmpty()){
				stopWordsArray.add(stopWordsLine);
			}
		}
		while((line = srcFileBR.readLine()) != null){
			resLine = lineProcess(line,stopWordsArray);
			if(!resLine.isEmpty()){
				//按行写，一行写一个单词
				String[] tempStr = resLine.split(" ");//\s
				for(int i = 0; i < tempStr.length; i++){
					if(!tempStr[i].isEmpty()){
						targetFileWriter.append(tempStr[i]+"\n");
					}
				}
			}
		}
		targetFileWriter.flush();
		targetFileWriter.close();
		srcFileReader.close();
		stopWordsReader.close();
		srcFileBR.close();
		stopWordsBR.close();	
	}
	
	/**对每行字符串进行处理，主要是词法分析、去停用词和stemming
	 * @param line 待处理的一行字符串
	 * @param ArrayList<String> 停用词数组
	 * @return String 处理好的一行字符串，是由处理好的单词重新生成，以空格为分隔符
	 * @throws IOException 
	 */
	private static String lineProcess(String line, ArrayList<String> stopWordsArray) throws IOException {
		// TODO Auto-generated method stub
		//step1 英文词法分析，去除数字、连字符、标点符号、特殊字符，所有大写字母转换成小写，可以考虑用正则表达式
		String res[] = line.split("[^a-zA-Z]");
		//这里要小心，防止把有单词中间有数字和连字符的单词 截断了，但是截断也没事
		String resString = new String();
		//step2去停用词
		//step3stemming,返回后一起做
		for(int i = 0; i < res.length; i++){
			if(!res[i].isEmpty() && !stopWordsArray.contains(res[i].toLowerCase())){
				resString += " " + res[i].toLowerCase() + " ";
			}
		}
		return resString;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public void BPPMain(String[] args) throws IOException {
		// TODO Auto-generated method stub
		DataPreProcess dataPrePro = new DataPreProcess();
		dataPrePro.doProcess("F:/DataMiningSample/orginSample");

	}

}
