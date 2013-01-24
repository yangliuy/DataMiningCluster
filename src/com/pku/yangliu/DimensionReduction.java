package com.pku.yangliu;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**基于LSI对文档的特征向量做降维，SVD运算基于JAMA矩阵运算包实现
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 *
 */
public class DimensionReduction {

	/**把测试样例的map转化成文档相似性矩阵
	 * @param Map<String, Map<String, Double>> allTestSampleMap 所有测试样例的<文件名，向量>构成的map
	 * @param String[] terms 特征词集合
	 * @return double[][] doc-doc相似性矩阵
	 * @throws IOException 
	 */
	public double[][] getSimilarityMatrix(
			Map<String, Map<String, Double>> allTestSampleMap, String[] terms) {
		// TODO Auto-generated method stub
		System.out.println("Begin compute docTermMatrix!");
		int i = 0;
		double [][] docTermMatrix = new double[allTestSampleMap.size()][terms.length];
		Set<Map.Entry<String, Map<String,Double>>> allTestSampleMapSet = allTestSampleMap.entrySet();
		for(Iterator<Map.Entry<String, Map<String,Double>>> it = allTestSampleMapSet.iterator();it.hasNext();){
			Map.Entry<String, Map<String,Double>> me = it.next();	
			for(int j = 0; j < terms.length; j++){
				if(me.getValue().containsKey(terms[j])){
					docTermMatrix[i][j] = me.getValue().get(terms[j]);
				}
				else {
					docTermMatrix[i][j] =0;
				}
			}
			i++;	
		}
	    double[][] similarityMatrix = couputeSimilarityMatrix(docTermMatrix);
		return similarityMatrix;
	}

	/**基于docTermMatrix生成相似性矩阵
	 * @param double[][] docTermMatrix doc-term矩阵
	 * @return double[][] doc-doc相似性矩阵
	 * @throws IOException 
	 */
	private double[][] couputeSimilarityMatrix(double[][] docTermMatrix) {
		// TODO Auto-generated method stub
		System.out.println("Compute docTermMatrix done! begin compute SVD");
		Matrix docTermM = new Matrix(docTermMatrix);
		SingularValueDecomposition s = docTermM.transpose().svd();
		System.out.println(" Compute SVD done!");
		//A*A' = D*S*S'*D'   如果是doc-term矩阵
		//A'*A = D*S'*S*D'   如果是term-doc矩阵
		//注意svd函数只适合行数大于列数的矩阵，如果行数小于列数，可对其转置矩阵做SVD分解
		Matrix D = s.getU();
		Matrix S = s.getS();
		for(int i = 100; i < S.getRowDimension(); i++){//降到100维
			S.set(i, i, 0);
		}
		System.out.println("Compute SimilarityMatrix done!");
		return D.times(S.transpose().times(S.times(D.transpose()))).getArray();
	}
}
