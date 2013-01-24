package com.pku.yangliu;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**聚类器主类，提供主函数入口
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 *
 */
public class ClusterMain {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		DataPreProcess DataPP = new DataPreProcess();
		ComputeWordsVector computeV = new ComputeWordsVector();
		//KmeansSVDCluster kmeansCluster1 = new KmeansSVDCluster();
		KmeansCluster kmeansCluster2 = new KmeansCluster();
		//DataPP.BPPMain(args);//数据预处理
		//下面创建聚类算法的测试样例集合
		String srcDir = "F:/DataMiningSample/processedSample_includeNotSpecial/";
		String destDir = "F:/DataMiningSample/clusterTestSample/";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		String beginTime = sdf.format(new java.util.Date());  
		System.out.println("程序开始执行时间:"+beginTime);  
		//String[] terms = computeV.createTestSamples(srcDir, destDir);
		//kmeansCluster1.KmeansClusterMain(destDir, terms);
		kmeansCluster2.KmeansClusterMain(destDir);
		String endTime = sdf.format(new java.util.Date());  
		System.out.println("程序结束执行时间:"+endTime);  
	}
}
