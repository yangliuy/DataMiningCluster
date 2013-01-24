package com.pku.yangliu;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.TreeSet;

/**计算文档的属性向量，将所有文档向量化
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 *
 */
public class ComputeWordsVector {
	
	/**计算文档的TF-IDF属性向量,返回Map<文件名，Map<特征词，TF-IDF值>>
	 * @param testSampleDir 处理好的聚类样本测试样例集合
	 * @return Map<String,Map<String,Double>> 所有测试样例的属性向量构成的map
	 * @throws IOException 
	 */
	public Map<String,Map<String,Double>> computeTFMultiIDF(String testSampleDir) throws IOException{
		String word;
		Map<String,Map<String,Double>> allTestSampleMap = new TreeMap<String,Map<String,Double>>();
		Map<String, Double> idfPerWordMap = computeIDF(testSampleDir);
		Map<String,Double> TFPerDocMap = new TreeMap<String,Double>();//计算每篇文档中含有各特征词数量
		File[] samples = new File(testSampleDir).listFiles();
		System.out.println("the total number of test files is" + samples.length);
		for(int i = 0; i < samples.length; i++){
			TFPerDocMap.clear();
			FileReader samReader = new FileReader(samples[i]);
			BufferedReader samBR = new BufferedReader(samReader);
			Double wordSumPerDoc = 0.0;//计算每篇文档的总词数
			while((word = samBR.readLine()) != null){
				if(!word.isEmpty()){
					wordSumPerDoc++;
					if(TFPerDocMap.containsKey(word)){
						Double count =  TFPerDocMap.get(word);
						TFPerDocMap.put(word, count + 1.0);
					}
					else {
						TFPerDocMap.put(word, 1.0);
					}
				}
			}

			Double maxCount = 0.0, wordWeight;//记录出现次数最多的词出现的次数，用做归一化
			Set<Map.Entry<String, Double>> tempTF = TFPerDocMap.entrySet();
			for(Iterator<Map.Entry<String, Double>> mt = tempTF.iterator(); mt.hasNext();){
				Map.Entry<String, Double> me = mt.next();
				if(me.getValue() > maxCount) maxCount = me.getValue();
			}
			for(Iterator<Map.Entry<String, Double>> mt = tempTF.iterator(); mt.hasNext();){
				Map.Entry<String, Double> me = mt.next();
				Double IDF = Math.log(samples.length / idfPerWordMap.get(me.getKey())) / Math.log(10);
				wordWeight =  (me.getValue() / maxCount) * IDF;
				TFPerDocMap.put(me.getKey(), wordWeight);
			}
			TreeMap<String,Double> tempMap = new TreeMap<String,Double>();
			tempMap.putAll(TFPerDocMap);
			allTestSampleMap.put(samples[i].getName(), tempMap);
		}
		//printTestSampleMap(allTestSampleMap);
		return allTestSampleMap;
	}
	
	/**输出测试样例map内容，用于测试
	 * @param SortedMap<String,Double> 属性词典
	 * @throws IOException 
	 */
	void printTestSampleMap(Map<String,Map<String,Double>> allTestSampleMap) throws IOException {
		// TODO Auto-generated method stub
		File outPutFile = new File("F:/DataMiningSample/KmeansClusterResult/allTestSampleMap.txt");
		FileWriter outPutFileWriter = new FileWriter(outPutFile);
		Set<Map.Entry<String,Map<String,Double>>> allWords = allTestSampleMap.entrySet();
		for(Iterator<Map.Entry<String,Map<String,Double>>> it = allWords.iterator(); it.hasNext();){
			Map.Entry<String,Map<String,Double>> me = it.next();
			outPutFileWriter.append(me.getKey() + " ");
			Set<Map.Entry<String,Double>> vecSet = me.getValue().entrySet();
			for(Iterator<Map.Entry<String, Double>> jt = vecSet.iterator(); jt.hasNext();){
				Map.Entry<String, Double> ne = jt.next();
				outPutFileWriter.append(ne.getKey() + " "+ ne.getValue() + " ");
			}
			outPutFileWriter.append("\n");
			outPutFileWriter.flush();
		}
		outPutFileWriter.close();
	}
	
	/**统计每个词的总的出现次数，返回出现次数大于n次的词汇构成最终的属性词典
	 * @param strDir 处理好的newsgroup文件目录的绝对路径
	 * @throws IOException 
	 */
	public SortedMap<String,Double> countWords(String strDir,Map<String, Double> wordMap) throws IOException{
		File sampleFile = new File(strDir);
		File [] sampleDir = sampleFile.listFiles();
		String word;
		for(int j = 0; j < sampleDir.length; j++){
			File[] sample = sampleDir[j].listFiles();
			for(int i = 0; i < sample.length; i++){
				if(sample[i].getName().contains("stemed")){
					FileReader samReader = new FileReader(sample[i]);
					BufferedReader samBR = new BufferedReader(samReader);
					while((word = samBR.readLine()) != null){
						if(!word.isEmpty() && wordMap.containsKey(word)){
							double count = wordMap.get(word) + 1;
							wordMap.put(word, count);
						}
						else {
							wordMap.put(word, 1.0);
						}
					}
				}	
			}
		}
	
		//去除停用词后，先用DF法选取特征词，后面再加入特征词的选取算法
		SortedMap<String,Double> newWordMap = new TreeMap<String,Double>();
		Set<Map.Entry<String,Double>> allWords = wordMap.entrySet();
		for(Iterator<Map.Entry<String,Double>> it = allWords.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			if(me.getValue() > 100){//DF法降维
				newWordMap.put(me.getKey(),me.getValue());
			}
		}
		return newWordMap;	
	}

	/**计算IDF，即属性词典中每个词在多少个文档中出现过
	 * @param testSampleDir 聚类算法测试样本所在目录
	 * @return 单词的IDFmap 格式为SortedMap<String,Double> 即<单词，包含该单词的文档数>
	 * @throws IOException 
	 */
	Map<String,Double> computeIDF(String testSampleDir) throws IOException {
		// TODO Auto-generated method stub
		Map<String,Double> IDFPerWordMap = new TreeMap<String,Double>();
		Set<String> alreadyCountWord = new HashSet<String>();//记下当前已经遇到过的该文档中的词
		String word;
		File[] samples = new File(testSampleDir).listFiles();
		for(int i = 0; i < samples.length; i++){
			alreadyCountWord.clear();
			FileReader tsReader = new FileReader(samples[i]);
			BufferedReader tsBR = new BufferedReader(tsReader);
			while((word = tsBR.readLine()) != null){
				if(!alreadyCountWord.contains(word)){
					if(IDFPerWordMap.containsKey(word)){
						IDFPerWordMap.put(word, IDFPerWordMap.get(word) + 1.0);
					}
					else IDFPerWordMap.put(word, 1.0);
					alreadyCountWord.add(word);					
				}
			}
		}
		return IDFPerWordMap;
	}
	
	/**创建聚类算法的测试样例集，主要是过滤出只含有特征词的文档写到一个目录下
	 * @param String srcDir 源目录，已经经过预处理但还没有过滤非特征词的文档目录
	 * @param String destDir 目的目录，聚类算法的测试样例目录
	 * @return String[] 创建测试样例集中特征词数组
	 * @throws IOException 
	 */
	String[] createTestSamples( String srcDir, String destDir) throws IOException {
		// TODO Auto-generated method stub
		SortedMap<String,Double> wordMap = new TreeMap<String,Double>();
		wordMap = countWords(srcDir, wordMap);
		System.out.println("special words map sizes:" + wordMap.size());
		String word, testSampleFile;
		File[] sampleDir = new File(srcDir).listFiles();
		for(int i = 0; i < sampleDir.length; i++){
			File[] sample = sampleDir[i].listFiles();
			for(int j = 0;j < sample.length; j++){	
				if(sample[j].getName().contains("stemed")){
					testSampleFile = destDir + sampleDir[i].getName()+"_"+sample[j].getName();
					FileReader samReader = new FileReader(sample[j]);
					BufferedReader samBR = new BufferedReader(samReader);
					FileWriter tsWriter = new FileWriter(new File(testSampleFile));
					while((word = samBR.readLine()) != null){
						if(wordMap.containsKey(word)){
							tsWriter.append(word + "\n");
						}
					}
					tsWriter.flush();
					tsWriter.close();	
				}
			}
		}
		//返回属性词典
		String [] terms = new String[wordMap.size()];
		int i = 0;
		Set<Map.Entry<String,Double>> allWords = wordMap.entrySet();
		for(Iterator<Map.Entry<String,Double>> it = allWords.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			terms[i] = me.getKey();
			i++;
		}
		return terms;
	}
	
	/**评估函数根据聚类结果文件统计熵和混淆矩阵
	 * @param clusterResultFile 聚类结果文件
	 * @param K 聚类数目
	 * @return double 聚类结果的熵值
	 * @throws IOException 
	 */
	double evaluateClusterRes(String clusterResultFile, int K) throws IOException {
		// TODO Auto-generated method stub
		Map<String,String> rightCate = new TreeMap<String,String>();
		Map<String,String> resultCate = new TreeMap<String,String>();
		FileReader crReader = new FileReader(clusterResultFile);
		BufferedReader crBR = new BufferedReader(crReader);
		String[] s;
		String line;
		while((line = crBR.readLine()) != null){
			s = line.split(" ");
			resultCate.put(s[0], s[1]);	
			//再把s[0]用_分片
			rightCate.put(s[0], s[0].split("_")[0]);
		}
		return computeEntropyAndConfuMatrix(rightCate,resultCate,K);//返回熵
	}
	
	/**计算混淆矩阵并且输出，返回熵
	 * @param rightCate 正确类目对应map
	 * @param resultCate 聚类结果对应map
	 * @return double 返回聚类的熵
	 * @throws IOException 
	 */
	private double computeEntropyAndConfuMatrix(Map<String, String> rightCate,
			Map<String, String> resultCate, int K) {
		// TODO Auto-generated method stub	
		int[][] confusionMatrix = new int[K][20];//K行20列，[i,j]表示聚类i中属于类目j的文件数
		//首先求出类目对应的数组索引
		SortedSet<String> cateNames = new TreeSet<String>();
		Set<Map.Entry<String, String>> rightCateSet = rightCate.entrySet();
		for(Iterator<Map.Entry<String, String>> it = rightCateSet.iterator(); it.hasNext();){
			Map.Entry<String, String> me = it.next();
			cateNames.add(me.getValue());
		}
		String[] cateNamesArray = cateNames.toArray(new String[0]);
		Map<String,Integer> cateNamesToIndex = new TreeMap<String,Integer>();
		for(int i = 0; i < cateNamesArray.length; i++){
			cateNamesToIndex.put(cateNamesArray[i],i);
		}
		for(Iterator<Map.Entry<String, String>> it = rightCateSet.iterator(); it.hasNext();){
			Map.Entry<String, String> me = it.next();
			confusionMatrix[Integer.parseInt(resultCate.get(me.getKey()))][cateNamesToIndex.get(me.getValue())]++;
		}
		//输出混淆矩阵
		double [] clusterSum = new double[K];//记录每个聚类的文件数
		double[] everyClusterEntropy = new double[K];//记录每个聚类的熵
		double clusterEntropy = 0;
		System.out.print("    ");
		for(int i = 0; i < 20; i++){
			System.out.print(i + "    ");
		}
		System.out.println();
		for(int i = 0; i < K; i++){
			System.out.print(i + "    ");
			for(int j = 0; j < 20; j++){
				clusterSum[i] += confusionMatrix[i][j];
				System.out.print(confusionMatrix[i][j]+"    ");
			}
			System.out.println();
		}
		System.out.println();
		for(int i = 0; i < K; i++){
			if(clusterSum[i] != 0){
				for(int j = 0; j < 20; j++){
					 double p = (double)confusionMatrix[i][j]/clusterSum[i];
					 if(p != 0){
						 everyClusterEntropy[i] += -p * Math.log(p);
					 }
				}
				clusterEntropy += clusterSum[i]/(double)rightCate.size() * everyClusterEntropy[i];
			}
		}
		return clusterEntropy;
	}

}
