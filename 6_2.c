#include <stdio.h>

//�@�e����int�̈́e����֧�ֵ����������^С���ұ�����H֧�ַ�ؓ����������ȱ�����H��;���H��չʾ��
struct EX_GCD { //s,t�������ʽ�еĂS����gcd��a,b�������
	int s;
	int t;
	int gcd;
};

struct EX_GCD extended_euclidean(int a, int b) {
	struct EX_GCD ex_gcd;
	if (b == 0) { //b=0�rֱ�ӽY�����
		ex_gcd.s = 1;
		ex_gcd.t = 0;
		ex_gcd.gcd = 0;
		return ex_gcd;
	}
	int old_r = a, r = b;
	int old_s = 1, s = 0;
	int old_t = 0, t = 1;
	while (r != 0) { //���Uչ�W������㷨�M��ѭ�h
		int q = old_r / r;
		int temp = old_r;
		old_r = r;
		r = temp - q * r;
		temp = old_s;
		old_s = s;
		s = temp - q * s;
		temp = old_t;
		old_t = t;
		t = temp - q * t;
	}
	ex_gcd.s = old_s;
	ex_gcd.t = old_t;
	ex_gcd.gcd = old_r;
	return ex_gcd;
	}

int main(void) {
	int a, b;
	printf("Please input two integers divided by a space.\n");
	scanf("%d%d", &a, &b);
	if (a < b) { //��a < b���t���Qa�cb�Ա����
		int temp = a;
		a = b;
		b = temp;
	}
	struct EX_GCD solution = extended_euclidean(a, b);
	printf("%d*%d+%d*%d=%d\n", solution.s, a, solution.t, b, solution.gcd);
	printf("Press any key to exit.\n");
	getchar();
	getchar();
	return 0;
}
