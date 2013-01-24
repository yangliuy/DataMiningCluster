package com.pku.yangliu;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.lang.Integer;


/**Kmeans聚类算法的实现类，将newsgroups文档集聚成10类、20类、30类,采用SVD分解
 * 算法结束条件:当每个点最近的聚类中心点就是它所属的聚类中心点时，算法结束
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 *
 */

public class KmeansSVDCluster {
	
	/**Kmeans算法主过程
	 * @param Map<String, Map<String, Double>> allTestSampleMap 所有测试样例的<文件名，向量>构成的map
	 * @param double [][] docSimilarityMatrix 文档与文档的相似性矩阵 [i,j]为文档i与文档j的相似性度量
	 * @param int K 聚类的数量
	 * @return Map<String,Integer> 聚类的结果  即<文件名，聚类完成后所属的类别标号>
	 * @throws IOException 
	 */
	private Map<String, Integer> doProcess(
			Map<String, Map<String, Double>> allTestSampleMap, double[][] docSimilarityMatrix, int K) {
		// TODO Auto-generated method stub
		//0、首先获取allTestSampleMap所有文件名顺序组成的数组
		String[] testSampleNames = new String[allTestSampleMap.size()];
		int count = 0, tsLength = allTestSampleMap.size();
		Set<Map.Entry<String, Map<String, Double>>> allTestSampeleMapSet = allTestSampleMap.entrySet();
		for(Iterator<Map.Entry<String, Map<String, Double>>> it = allTestSampeleMapSet.iterator(); it.hasNext(); ){
			Map.Entry<String, Map<String, Double>> me = it.next();
			testSampleNames[count++] = me.getKey();
		}
		//1、初始点的选择算法是随机选择或者是均匀分开选择，这里采用后者
		Map<Integer, double[]> meansMap = getInitPoint(testSampleNames, docSimilarityMatrix, K);//保存K个中心点
		//2、初始化K个聚类
		int [] assignMeans = new int[tsLength];//记录所有点属于的聚类序号，初始化全部为0
		Map<Integer, Vector<Integer>> clusterMember = new TreeMap<Integer,Vector<Integer>>();//记录每个聚类的成员点序号
		Vector<Integer> mem = new Vector<Integer>();
		int iterNum = 0;//迭代次数
		while(true){
			System.out.println("Iteration No." + (iterNum++) + "----------------------");
			//3、找出每个点最近的聚类中心
			int[] nearestMeans = new int[tsLength];
			for(int i = 0; i < tsLength; i++){
				nearestMeans[i] = findNearestMeans(meansMap, i);
			}
			//4、判断当前所有点属于的聚类序号是否已经全部是其离得最近的聚类，如果是或者达到最大的迭代次数，那么结束算法
			int okCount = 0;
			for(int i = 0; i <tsLength; i++){
				if(nearestMeans[i] == assignMeans[i]) okCount++;
			}
			System.out.println("okCount = " + okCount);
			if(okCount == tsLength || iterNum >= 25) break;//最大迭代次数1000次
			//5、如果前面条件不满足，那么需要重新聚类再进行一次迭代，需要修改每个聚类的成员和每个点属于的聚类信息
			clusterMember.clear();
			for(int i = 0; i < tsLength; i++){
				assignMeans[i] = nearestMeans[i];
				if(clusterMember.containsKey(nearestMeans[i])){
					clusterMember.get(nearestMeans[i]).add(i);	
				}
				else {
					mem.clear();
					mem.add(i);
					Vector<Integer> tempMem = new Vector<Integer>();
					tempMem.addAll(mem);
					clusterMember.put(nearestMeans[i], tempMem);
				}
			}
			//6、重新计算每个聚类的中心点
			for(int i = 0; i < K; i++){
				if(!clusterMember.containsKey(i)){//注意kmeans可能产生空聚类
					continue;
				}
				double[] newMean = computeNewMean(clusterMember.get(i), docSimilarityMatrix);
				meansMap.put(i, newMean);
			}
		}
		
		//7、形成聚类结果并且返回
		Map<String, Integer> resMap = new TreeMap<String, Integer>();
		for(int i = 0; i < tsLength; i++){
			resMap.put(testSampleNames[i], assignMeans[i]);
		}
		return resMap;
	}

	/**计算新的聚类中心与每个文档的相似度
	 * @param clusterM 该聚类包含的所有文档的序号
	 * @param double [][] docSimilarityMatrix 文档之间的相似度矩阵
	 * @return double[] 新的聚类中心与每个文档的相似度
	 * @throws IOException 
	 */
	private double[] computeNewMean(Vector<Integer> clusterM,
			double [][] docSimilarityMatrix) {
		// TODO Auto-generated method stub
		double sim;
		double [] newMean = new double[docSimilarityMatrix.length];
		double memberNum = (double)clusterM.size();
		for(int i = 0; i < docSimilarityMatrix.length; i++){
			sim = 0;
			for(Iterator<Integer> it = clusterM.iterator(); it.hasNext();){
				sim += docSimilarityMatrix[it.next()][i];
			}
			newMean[i] = sim / memberNum;
		}
		return newMean;
	}

	/**找出距离当前点最近的聚类中心
	 * @param Map<Integer, double[]> meansMap 中心点Map value为中心点和每个文档的相似度
	 * @param int m
	 * @return i 最近的聚类中心的序 号
	 * @throws IOException 
	 */
	private int findNearestMeans(Map<Integer, double[]> meansMap ,int m) {
		// TODO Auto-generated method stub
		double maxSim = 0;
		int j = -1;
		double[] simArray;
		Set<Map.Entry<Integer, double[]>> meansMapSet = meansMap.entrySet();
		for(Iterator<Map.Entry<Integer, double[]>> it = meansMapSet.iterator(); it.hasNext();){
			Map.Entry<Integer, double[]> me = it.next();
			simArray = me.getValue();
			if(maxSim < simArray[m]){
				maxSim = simArray[m];
				j = me.getKey();
			}
		}
		return j;
	}

	/**获取kmeans算法迭代的初始点
	 * @param k 聚类的数量
	 * @param String[] testSampleNames 测试样例文件名数组
	 * @param double[][] docSimilarityMatrix 文档相似性矩阵
	 * @return Map<Integer, double[]> 初始中心点容器 key是类标号，value为该类与其他文档的相似度数组
	 * @throws IOException 
	 */
	private Map<Integer, double[]> getInitPoint(String[] testSampleNames, double[][] docSimilarityMatrix, int K) {
		// TODO Auto-generated method stub
		int i = 0;
		Map<Integer, double[]> meansMap = new TreeMap<Integer, double[]>();//保存K个聚类中心点向量
		System.out.println("本次聚类的初始点对应的文件为：");
		for(int count = 0; count < testSampleNames.length; count++){
			if(count == i * testSampleNames.length / K){
				meansMap.put(i, docSimilarityMatrix[count]);
				System.out.println(testSampleNames[count]);
				i++;
			}
		}
		return meansMap;
	}

	/**输出聚类结果到文件中
	 * @param kmeansClusterResultFile 输出文件目录
	 * @param kmeansClusterResult 聚类结果
	 * @throws IOException 
	 */
	private void printClusterResult(Map<String, Integer> kmeansClusterResult, String kmeansClusterResultFile) throws IOException {
		// TODO Auto-generated method stub
		FileWriter resWriter = new FileWriter(kmeansClusterResultFile);
		Set<Map.Entry<String,Integer>> kmeansClusterResultSet = kmeansClusterResult.entrySet();
		for(Iterator<Map.Entry<String,Integer>> it = kmeansClusterResultSet.iterator(); it.hasNext(); ){
			Map.Entry<String, Integer> me = it.next();
			resWriter.append(me.getKey() + " " + me.getValue() + "\n");
		}
		resWriter.flush();
		resWriter.close();
	}
	
	/**Kmeans算法
	 * @param String testSampleDir 测试样例目录
	 * @param String[] term 特征词数组
	 * @throws IOException 
	 */
	public void KmeansClusterMain(String testSampleDir, String[] terms) throws IOException {
		//首先计算文档TF-IDF向量，保存为Map<String,Map<String,Double>> 即为Map<文件名，Map<特征词，TF-IDF值>>
		ComputeWordsVector computeV = new ComputeWordsVector();
		DimensionReduction dimReduce = new DimensionReduction();
		int[] K = {10, 20, 30};
		Map<String,Map<String,Double>> allTestSampleMap = computeV.computeTFMultiIDF(testSampleDir);
		//基于allTestSampleMap生成一个doc*term矩阵，然后做SVD分解
		double[][] docSimilarityMatrix = dimReduce.getSimilarityMatrix(allTestSampleMap, terms);
		for(int i = 0; i < K.length; i++){
			System.out.println("开始聚类，聚成" + K[i] + "类");
			String KmeansClusterResultFile = "F:/DataMiningSample/KmeansClusterResult/";
			Map<String,Integer> KmeansClusterResult = new TreeMap<String, Integer>();
			KmeansClusterResult = doProcess(allTestSampleMap, docSimilarityMatrix, K[i]);
			KmeansClusterResultFile += K[i];
			printClusterResult(KmeansClusterResult,KmeansClusterResultFile);
			System.out.println("The Entropy for this Cluster is " + computeV.evaluateClusterRes(KmeansClusterResultFile, K[i]));
		}
	}
}

