#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<sstream>
#include<gmp.h>

int main()
{
//Compute d and N given p, q, and e
  char inputPQE;
  char outputDN;
  char inputX;
  char outputEX;
  char inputC;
  char outputDC;
  char a, pStr, qStr, eStr;
  int b;
  int k;
  mpz_t p, q, e, d, n, fin, x, c, z;
  mpz_init(p);
  mpz_init(q);
  mpz_init(e);
  mpz_init(d);
  mpz_init(n);
  mpz_init(fin);
  mpz_init(x);
  mpz_init(c);
  mpz_init(z);


//Get file names and number of bits for N
  printf("Enter the name of the file that contains p, q, and e: ");
  scanf("%s", &inputPQE);
  printf("Enter the output file name to store d and N: ");
  scanf("%s", &outputDN);
  printf("Enter the number of bits of N: ");
  scanf ("%d", &k);
  mpz_init2(n, k);
  
  
//Read p, q, e from specified file
  FILE *input1;
  input1 = fopen(&inputPQE, "r");
  fscanf(input1, "%s", &pStr);
  fscanf(input1, "%s", &qStr);
  fscanf(input1, "%s", &eStr);
  mpz_set_str(p, &pStr, 10);
  mpz_set_str(q, &qStr, 10);
  mpz_set_str(e, &eStr, 10);
  int fclose(FILE *input1);


//Compute d and N using the given p, q, and e
  mpz_mul(n, p, q);
  mpz_sub_ui(p, p, 1UL);
  mpz_sub_ui(q, q, 1UL);
  mpz_mul(fin, p, q);
  mpz_invert(d, e, fin);


//Output d and N to specified file  
  FILE *output1;
  output1 = fopen(&outputDN, "w");
  fprintf(output1, "%ld\n", mpz_get_ui(d));
  fprintf(output1, "%ld\n", mpz_get_ui(n));
  int fclose(FILE *output1);


//Encrypt x and output the value

//Get file names
  printf("Enter the name of the file that contains x to be encrypted: ");
  scanf("%s", &inputX);
  printf("Enter the output file name to store E(x): ");
  scanf("%s", &outputEX);
 
  
//Read in x
  FILE *input2;
  input2 = fopen(&inputX, "r");
  mpz_inp_str(x, input2, 10);
  int fclose(FILE *input2);
 
  
//Encrypt x
  mpz_powm_ui(c, x, mpz_get_ui(e), n);
 
  
//Output cipher to file
  FILE *output2;
  output2 = fopen(&outputEX, "w");
  mpz_inp_str(c, output2, 10);
  int fclose(FILE *output2);


//Decrypt c

//Get file names
  printf("Enter the name of the file that contains c to be decrypted: ");
  scanf("%s", &inputC);
  printf("Enter the output file name to store D(c): ");
  scanf("%s", &outputDC);
 
  
//Read in c
  FILE *input3;
  input3 = fopen(&inputC, "r");
  mpz_inp_str(c, input3, 10);
  int fclose(FILE *input3);
 
  
//Decrypt x
  mpz_powm_ui(x, c, mpz_get_ui(d), n);
 
  
//Output cipher to file
  FILE *output3;
  output3 = fopen(&outputDC, "w");
  mpz_inp_str(x, output3, 10);
  int fclose(FILE *output3);
  
  return 0;
}
