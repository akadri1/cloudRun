import java.util.concurrent.ExecutionException;

public class CPUBenchmarkFloat{
	public static final int totalRun=240000000;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		//Float
		System.out.print("\n---------Floating point CPU--------");
		runBenchmarkFloat();
	}

	public static void runBenchmarkFloat() throws InterruptedException, ExecutionException {
		long start = System.nanoTime();
		floatCPU(2,1.0 + 1e-8,  totalRun);
		long end = System.nanoTime();
		System.out.print("\tthreadTime:"+(end-start));
		System.out.print("\tnoOfThreads:"+totalRun);
		System.out.print("\tThreadGFLOP:"+totalRun/(end-start));
	}

	static double floatCPU(double add, double mul, int totalIteration) {
		double GFlops=0;

		double sum1=0, 
				sum2=0.1, 
				sum3=0.2, 
				sum4=-0.2, 
				sum5=-0.1, 
				sum6=-0.2;
		double mul1=1, 
				mul2= 1.1, 
				mul3=1.2, 
				mul4= -1.2, 
				mul5=-1.1, 
				mul6 =-1;
		int instructionCycle=totalIteration/10;
		long start = System.nanoTime();
		for(long i=1; i <=instructionCycle; i++){
			sum1+=add; 
			sum2+=add; 
			sum3+=add; 
			mul1*=mul; 
			mul2*=mul; 
			mul3*=mul; 
			sum4+=add; 
			sum5+=add; 
			sum6+=add;
			mul4*=mul; 
		}
		long end = System.nanoTime();

		long total = end-start;
		GFlops = totalIteration/total;
		System.out.print("\niteration:"+totalIteration+"\ttotal (ms):"+ total/1000000);
		System.out.print("\tfloatCPU_GFlops: "+GFlops);
		return (sum1+mul1+sum2+mul2+sum3+mul3+sum4+mul4+sum5+mul5+sum6+mul6);
	}	
}

