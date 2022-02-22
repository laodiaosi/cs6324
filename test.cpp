//����GMP���������Ļ���������
#define _CRT_SECURE_NO_WARNINGS
#include<stdio.h>
#include<gmp.h>
#include<string>
#include<stdlib.h>
#include<time.h>
#include<windows.h>
#define N 1000000
#define M 2000
using namespace std;

/*
  gmp�﷨�ο���
  https://www.cnblogs.com/y3w3l/p/5947450.html
  https://www.cnblogs.com/ECJTUACM-873284962/p/8350320.html
  https://blog.csdn.net/iteye_17686/article/details/82310869?ops_request_misc=&request_id=&biz_id=102&utm_term=mpz_init_set_str&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-5-.nonecase&spm=1018.2226.3001.4187
*/

/*��ŷ������*/
void GetEulerValue(mpz_t p, mpz_t q,mpz_t *euler)
{
	mpz_t i, j,temp;
	mpz_init(i);                        //��ʼ��i,j
	mpz_init(j);
	mpz_init_set_str(temp, "1", 10);    //temp��ʼ����ֵΪ1(���ַ�����ʼ��Ϊgmp������)
	mpz_sub(i, p, temp);                //i = p - temp����i=p-1
	mpz_sub(j, q, temp);                //j = q - temp����j=q-1
	mpz_mul(*euler, j, i);              //*euler=j * i
	mpz_clear(i);
	mpz_clear(j);
	mpz_clear(temp);
}

/*������ɴ��������ο���https://www.cnblogs.com/y3w3l/p/5947450.html */
void CreatePrime(mpz_t *rand_p, mpz_t *rand_q, int judge, mpz_t euler,int range)
{
	unsigned long seed = time(NULL);
	mpz_t temp, i;

	mpz_init(temp);
	mpz_init_set_str(i, "1", 10);

	//��һ�����״̬�Ķ��弴��ʼ��
	gmp_randstate_t state;
	gmp_randinit_default(state);

	gmp_randseed_ui(state, seed);           //�������״̬������

	int BOOL = 0;
	switch (judge)
	{
	case 0:
		while (BOOL == 0)
		{
			mpz_urandomb(*rand_p, state, range);   //���������
			BOOL = mpz_probab_prime_p(*rand_p, 15);//�ж��Ƿ�������������BOOL==1����������
		}
		BOOL = 0;
		while (BOOL == 0)
		{
			mpz_nextprime(*rand_q, *rand_p);
			BOOL = mpz_probab_prime_p(*rand_q, 15);//�ж��Ƿ�������������BOOL==1����������
		}
		break;
	case 1:
		while (BOOL == 0)
		{
			//���������
			mpz_urandomm(*rand_p, state, euler);
			//��rand��euler�����Լ��
			mpz_gcd(temp, *rand_p, euler);
			//�ж��Ƿ����㻥������
			BOOL = (mpz_cmp(temp, i)==0);
		}
		break;
	}
	mpz_clear(temp);
	mpz_clear(i);
}

/*�������㷨ѧϰ��https://blog.csdn.net/chen_zan_yu_/article/details/90522763?ops_request_misc=&request_id=&biz_id=102&utm_term=%E5%BF%AB%E9%80%9F%E5%B9%82%E5%8F%96%E6%A8%A1%E7%AE%97%E6%B3%95&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-3-.pc_search_result_before_js&spm=1018.2226.3001.4187
int PowerMod(int a, int b, int c)
{
	int ans = 1;
	a = a % c;
	while(b>0) {
		if(b % 2 = = 1)
		ans = (ans * a) % c;
		b = b/2;
		a = (a * a) % c;
	}
	return ans;
}*/

void Encrypt(mpz_t *result,mpz_t n, mpz_t m, mpz_t e)
{
	mpz_t i,j,k,l;                  //Ϊ�˲���gmp��Ĵ������㣬��ҪһЩ�߾��ȱ����洢һЩ����

	mpz_init_set_str(i, "2", 10);     //��i������2
	mpz_init_set_str(j, "1", 10);     //��j������1
	mpz_init_set_str(l, "0", 10);     //��l������0
	mpz_init(k);
	mpz_set(*result, j);

	mpz_mod(m, m, n);             //(a*b) mod c = ((a mod c)*(b mod c)) mod c
	while (mpz_cmp(e, l)>0)
	{

		if (mpz_odd_p(e))             //�ж��Ƿ�������
		{
			/* result=(result*temp) mod euler */
			mpz_mul(*result, *result, m);
			mpz_mod(*result, *result, n);
		}
		mpz_div(e, e, i);
		mpz_mul(m, m, m);
		mpz_mod(m, m, n);
	}
	mpz_clear(i);
	mpz_clear(j);
	mpz_clear(k);
	mpz_clear(l);
}

/*
��չŷ������㷨�ο�ѧϰ��
https://blog.csdn.net/qq_16964363/article/details/79251433?ops_request_misc=&request_id=&biz_id=102&utm_term=%E6%B1%82%E4%B9%98%E6%B3%95%E9%80%86%E5%85%83&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-6-.nonecase&spm=1018.2226.3001.4187
https://blog.csdn.net/my_acm/article/details/38423553?ops_request_misc=&request_id=&biz_id=102&utm_term=%E6%B1%82%E4%B9%98%E6%B3%95%E9%80%86%E5%85%83&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-3-.nonecase&spm=1018.2226.3001.4187
https://blog.csdn.net/qq_21120027/article/details/52921404?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522162201937316780262575134%2522%252C%2522scm%2522%253A%252220140713.130102334..%2522%257D&request_id=162201937316780262575134&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduend~default-1-52921404.pc_search_result_before_js&utm_term=%E6%B1%82%E4%B9%98%E6%B3%95%E9%80%86%E5%85%83&spm=1018.2226.3001.4187
https://www.jianshu.com/p/9a5f60efeb0d
*/

void Get_d(mpz_t e, mpz_t euler,mpz_t *temp,mpz_t *a,mpz_t *b)
{
	mpz_t zero,one,i;
	mpz_init_set_str(zero, "0", 10);
	mpz_init_set_str(one, "1", 10);
	mpz_init(i);
	if (mpz_cmp(zero, euler) == 0)
	{
		mpz_set(*temp, e);
		mpz_set(*a, one);
		mpz_set(*b, zero);
		return;
	}
	mpz_mod(i, e, euler);
	Get_d(euler,i,temp,b,a);
	mpz_div(i, e, euler);
	mpz_mul(i, i, *a);
	mpz_sub(*b, *b, i);
	mpz_clear(zero);
	mpz_clear(one);
	mpz_clear(i);
}
int main()
{
	system("title MADE BY YQC");
	char *temp_p = (char*)malloc(sizeof(char*)*N),
		 *temp_q = (char*)malloc(sizeof(char*)*N),
		 *temp_m = (char*)malloc(sizeof(char*)*N),
		 *temp_n = (char*)malloc(sizeof(char*)*N),
		 *temp_e = (char*)malloc(sizeof(char*)*N);
	mpz_t p, q, e, d, euler, c, m, x, y, temp,n;
	int base=10;                                   //Ĭ��10��������

	mpz_init(euler);
	mpz_init(c);
	mpz_init(d);
	mpz_init(x);
	mpz_init(y);
	mpz_init(temp);
	mpz_init(n);

	int operation,range,ch='\0';
	while (1)
	{
		system("CLS");
		printf("��ע�⣺�������������������ԿĬ�϶��������ݶ��룬�������ַ������ܣ���Ҫ����ת����ASCIIֵ�����룬�������'0'���ܣ����������Ӧ����'0'��Ӧ��ASCIIֵ48��\n1.������ɴ�����p,q,e\n2.�ֶ����������p,q,e\n3.�˳�����\n��ѡ��Ҫ���еĲ���:");
		scanf("%d", &operation);
		switch (operation)
		{
		case 1:
			system("CLS");
			mpz_init_set_str(p,"0",10);
			mpz_init_set_str(q,"0",10);
			mpz_init_set_str(e,"0",10);
			printf("����������m:");
			scanf("%s", temp_m);
			mpz_init_set_str(m, temp_m, base);

			CreatePrime(&p, &q, 0, euler,M);
			gmp_printf("\n��(���� ��)��:�����ɲ��Դ�����p:\n%Zd\n", p);
			gmp_printf("��(���� ��)��:�����ɲ��Դ�����q:\n%Zd\n", q);
			mpz_mul(n, p, q);
			gmp_printf("��(���� ��)��:�����n=\n%Zd\n", n);
			GetEulerValue(p, q, &euler);
			CreatePrime(&e, &q, 1, euler,M);
			gmp_printf("��(���� ��)��:�����ɲ��Թ�Կe:\n%Zd\n", e);
			Get_d(e, euler, &temp, &x, &y);
			mpz_add(d, euler, x);
			mpz_mod(d, d, euler);
			gmp_printf("��(���� ��)��:�����˽ԿdΪ:\n%Zd\n", d);
			Encrypt(&c, n, m, e);
			gmp_printf("��(���� ��)��:���������cΪ:\n%Zd\n", c);
			label_3:
			gmp_printf("��(���� ��)��:�Ƿ����(Y/N):\n");
			scanf_s("%c", &ch);
			if(ch=='y'||ch=='Y') break;
			else goto label_3;
		case 2:
			system("CLS");
			printf("����������m:");
			scanf("%s", temp_m);
			printf("������ȫ�����ݲ��õĽ���:");
			scanf("%d", &base);
			mpz_init_set_str(m, temp_m, base);

			printf("1.��˽Կ��ȡģʽ������p��q���⽫�������˽Կ������\n2.�����ܻ�ģʽ������n���⽫ֱ���������\n��ѡ��ģʽ:");
			scanf("%d",&operation);
			switch (operation)
			{
			case 1:
				label_1:
				gmp_printf("�����������p:");
				scanf("%s", temp_p);
				mpz_init_set_str(p, temp_p, base);
				if (!mpz_probab_prime_p(p, 10))  goto label_p;

				label_2:
				gmp_printf("�����������q:");
				scanf("%s", temp_q);
				mpz_init_set_str(q, temp_q, base);
				if (!mpz_probab_prime_p(q, 10))  goto label_q;

				mpz_mul(n, p, q);
				gmp_printf("��(���� ��)��:�Ѽ����n=%Zd\n", n);

				GetEulerValue(p, q, &euler);
				gmp_printf("��(���� ��)��:�Ѽ����n��ŷ������Euler=%Zd\n", euler);

				gmp_printf("�����빫Կe��ȷ��gcd(Euler,e)==1��:");
				scanf("%s", temp_e);
				mpz_init_set_str(e, temp_e, base);

				Get_d(e, euler, &temp, &x, &y);
				mpz_add(d, euler, x);
				mpz_mod(d, d, euler);
				gmp_printf("��(���� ��)��:�Ѽ����˽ԿdΪ��%Zd\n", d);

				Encrypt(&c, n, m, e);
				gmp_printf("��(���� ��)��:���ռ��������cΪ:%Zd\n", c);
				break;
			case 2:
				gmp_printf("�����빫Կn:");
				scanf("%s", temp_n);
				mpz_init_set_str(n, temp_n, base);

				gmp_printf("�����빫Կe��ȷ��gcd(Euler,e)==1��:");
				scanf("%s", temp_e);
				mpz_init_set_str(e, temp_e, base);

				Encrypt(&c, n, m, e);
				gmp_printf("��(���� ��)��:���ռ��������cΪ:%Zd\n", c);
				break;
			}
		label_4:
			gmp_printf("�Ƿ������Y\N��:");
			scanf("%c", &ch);
			if (ch == 'y' || ch == 'Y') break;
			else goto label_4;
		case 3:
			mpz_clear(p);
			mpz_clear(q);
			mpz_clear(e);
			mpz_clear(d);
			mpz_clear(euler);
			mpz_clear(c);
			mpz_clear(m);
			mpz_clear(x);
			mpz_clear(y);
			mpz_clear(temp);
			mpz_clear(n);
			free(temp_m);
			free(temp_p);
			free(temp_q);
			free(temp_e);
			free(temp_n);
			exit(0);
			break;
		default:
			printf("\n�������󣡣���");
			break;
		}
	}
label_p:
	printf("�����p��������������������\n");
	goto label_1;
label_q:
	printf("�����q��������������������\n");
	goto label_2;
	return 0;
