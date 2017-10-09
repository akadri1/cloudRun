#include <stdio.h>
#include <time.h>
#include <math.h>
#include <string.h>
#include <pthread.h>
#include <stdlib.h>

//Function declaration

int threadSequential(int k);
int threadRandom(int k);
void *random_RW_Benchmark(void *n);
void *sequential_RW_Benchmark(void *n);
int thread_10MB(int k);
void printResult(long long int Block_size, long double compute_time, int threadCount);
void callRmemcpy(char *destination, char* source, long long int *BlockSize);

//Global variable declaration

struct timespec startop, endop;
long double compute_time, throughput;
long double latency;
time_t s,e;
pthread_t threads[8];

int main(){
	int i,k,choice,n=0,p=0;
	printf("Enter the choice of operation\n");
	printf("1.Random Read Write Operation\n");
	printf("2.Sequential Read Write Operation\n");
	scanf("%d",&choice);
	switch(choice)
	{
	case 1:
		printf("-----For Random Access-----\n");
		for(k=0;k<4;k++) 						//calling the thread program to make threads for Random R/W
		{
			n=pow(2,k);
			threadRandom(n);
		}


		break;
	case 2 :
		printf("-----For Sequential Access-----\n");
		for(i=0;i<4;i++)
		{
			p=pow(2,i);
			threadSequential(p);				//calling the thread program to make threads for Sequential R/W
		}
		break;
	default:
		printf("Enter one choice");
	}

}
int threadRandom(int k)
{
	long long int n;
	int i=1,j=1;
	while(i<4){
		n=pow(1024,i-1);

		clock_gettime(CLOCK_MONOTONIC, &startop);
		s  = startop.tv_sec;
		j=1;
		while(j<=k)
		{

			pthread_create(&threads[j], NULL, random_RW_Benchmark, &n);	// Create threads for random R/W benchmarking
			j++;
		}
		j=1;
		while(j<=k)
		{
			pthread_join(threads[j], NULL);								// joining the threads
			j++;
		}

		clock_gettime(CLOCK_MONOTONIC, &endop);
		e  = endop.tv_sec;

		compute_time = (1000000.0f*(e-s) + (endop.tv_nsec - startop.tv_nsec)/1000.0f);	// computing the time taken to perform operation

		printResult( n,compute_time,k);									// calling printResult function to print latency and throughput
		i++;
		if(n == 1024 *1024)												// if 1MB reached then call for 10 MB
		{
			thread_10MB(k);
		}  }return 0;
}
int thread_10MB(int k)													// creating threads for R/W on 10MB block size
{
	long long int n;
	int j;
	n=1024*1024*10;
	clock_gettime(CLOCK_MONOTONIC, &startop);
	s  = startop.tv_sec;
	j=1;
	while(j<=k)
	{
		pthread_create(&threads[j], NULL, sequential_RW_Benchmark, &n);
		j++;
	}
	j=1;
	while(j<=k)
	{
		pthread_join(threads[j], NULL);
		j++;
	}

	clock_gettime(CLOCK_MONOTONIC, &endop);
	e  = endop.tv_sec;

	compute_time =  (1000000.0f*(e-s) + (endop.tv_nsec - startop.tv_nsec)/1000.0f);	// computing the time taken to perform operation

	if(n==1)
	{
		latency= compute_time/2000;
		printf("Thread No. %d\t Block size is %lld\t\t latency    is \t%7Le us\n",k,n,latency);

	}
	else{
		throughput= ((n * k *100)/(1024.0 * 1024.0 )) / (compute_time/1000000.0);
		printf("Thread No. %d\t Block size is %lld\t\t throughput is  %LeMBps\n",k,n,throughput);
	}
	return 0;
}
int threadSequential(int p)
{
	long long int n;
	int i=1,j=1;
	while(i<4){
		n=pow(1024,i-1);
		clock_gettime(CLOCK_MONOTONIC, &startop);
		s  = startop.tv_sec;
		j=1;
		while(j<=p)
		{
			pthread_create(&threads[j], NULL, sequential_RW_Benchmark, &n);	// Create threads for random R/W benchmarking
			j++;
		}
		for(j=1;j<=p;j++)
		{
			pthread_join(threads[j], NULL);							// joining the threads
		}

		clock_gettime(CLOCK_MONOTONIC, &endop);
		e  = endop.tv_sec;

		compute_time =  (1000000.0f*(e-s) + (endop.tv_nsec - startop.tv_nsec)/1000.0f);		// computing the time taken to perform operation

		printResult( n,compute_time,p);						// calling printResult function to print latency and throughput
		i++;
		if(n == 1024 *1024)									// if 1MB reached then call for 10 MB
		{
			thread_10MB(p);
		}
	}return 0;
}
void printResult(long long int n, long double compute_time, int p)
{
	if(n==1)
	{
		latency= compute_time/2000;
		printf("Thread No. %d\t  Block size is %lld\t\t latency    is \t%7Le us\n",p,n,latency);

	}
	else
	{
		throughput= ((p *n *100)/(1024.0 * 1024.0)) / (compute_time/1000000.0);
		printf("Thread No. %d\t Block size is %lld\t\t throughput is \t%LeMBps\n",p,n,throughput);
	}
}


void *random_RW_Benchmark(void *n){
	//size_t _msize;
	long int *size = (long int *)n;
	char * destination = (char *)malloc(100*(*size) * sizeof(char));	 //destinationination memory allocation
	char * source = (char *)malloc(100*(*size) * sizeof(char));
	//_msize= *destination;
	//printf("size of block is %u\n ",((size_t *)destination)[-1]);//source memory allocation
	void callRmemcpy(char * destination, char * source, long long int *size); //call to copy from source to destinationination randomly
	free(destination);
	free(source);
	return 0;
}

void *sequential_RW_Benchmark(void* n){
	long	int *size = (long int *)n;
	int k = 0;
	char * destination = (char *)malloc(100*(*size)* sizeof(char)); 	//destinationination memory allocation
	char * source = (char *)malloc( 100* (*size) * sizeof(char)); 		//source memory allocation
	for (int j = 0; j < 100; j++) {
		memcpy(destination +k , source +k, *size) ;					//copying from source to destination sequentially
		k =k++;
	}
	free(destination);
	free(source);
	return 0;
}
void callRmemcpy(char * destination, char * source, long long int *BlockSize)
{	long int random= rand()%100;									// Random number generator
for(int i=0;i<100;i++)
	memcpy(destination+random, source+random, *BlockSize);	
}

