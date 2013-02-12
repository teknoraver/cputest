#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <time.h>

#define dim 10 //Change as required
 
double matrix[dim][dim];
 
int determinant(int n, double mat[n][n])
{
	int i,j,i_count,j_count, count=0;
	double array[n-1][n-1], det=0;
 
	if(n<1) {
		puts("Error");
		exit(1);
	}
	if(n==1)
		return mat[0][0];
	if(n==2)
		return (mat[0][0]*mat[1][1] - mat[0][1]*mat[1][0]);
 
	for(count=0; count<n; count++) {
		//Creating array of Minors
		i_count=0;
		for(i=1; i<n; i++) {
			j_count=0;
			for(j=0; j<n; j++) {
				if(j == count)
					continue;
				array[i_count][j_count] = mat[i][j];
				j_count++;
			}
			i_count++;
		}
		det += pow(-1, count) * mat[0][count] * determinant(n-1,array);	//Recursive call
	}
	return det;
}
 
int main()
{
	int i, j;
	srand48(time(0));
	for(i = 0; i < dim; i++) {
		for(j = 0; j < dim; j++) {
			matrix[i][j] = drand48() * 1000000;
			printf("%g ", matrix[i][j]);
		}
		puts("\n");
	}
	double x = determinant(dim, matrix);
	printf("Determinant: %g\n", x);

	return 0;
}