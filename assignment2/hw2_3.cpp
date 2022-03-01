#include <iostream>
#include <math.h>
#include <gmp.h>

using namespace std;

int main(){
  mpz_t p,q,rop,n,fin,x,c;
  mpz_init(rop);
  //mpz_init(ed);
  mpz_init(p);
  mpz_init(q);
  //mpz_init(e);
  //mpz_init(d);
  mpz_init(n);
  mpz_init(fin);
  mpz_init(x);
  mpz_init(c);
  mpz_init_set_str(p,"9967",10);
  mpz_init_set_str(q,"6073",10);
  mpz_init_set_str(p,"9967",10);
  //mpz_init_set_str(e,"31",10);
  //mpz_init_set_str(d,"29280751",10);
  int e = 31;
  int d = 29280751;
  int ed = e*d;
  mpz_init_set_str(n,"60529519",10);
  mpz_init_set_str(fin,"60513552",10);
  int N=60529519;
  mpz_t i;
  mpz_init(i);
  mpz_init_set_str(i,"10000",10);

  mpz_t i2;
  mpz_init(i2);
  mpz_init_set_str(i2,"20000",10);

  mpz_t i3;
  mpz_init(i3);
  mpz_init_set_str(i3,"30000",10);

  mpz_t i4;
  mpz_init(i4);
  mpz_init_set_str(i4,"40000",10);

  mpz_t i5;
  mpz_init(i5);
  mpz_init_set_str(i5,"50000",10);

  mpz_t i6;
  mpz_init(i6);
  mpz_init_set_str(i6,"60000",10);

      
      //mpz_ui_pow_ui(rop,i,ed);
      //mpz_pow_ui(rop,i,ed);
      mpz_powm_ui(rop,i,d,n);
      gmp_printf("%Zd\n",rop);
      mpz_powm_ui(rop,i2,d,n);
      gmp_printf("%Zd\n",rop);
     mpz_powm_ui(rop,i3,d,n);
      gmp_printf("%Zd\n",rop);
      mpz_powm_ui(rop,i4,d,n);
      gmp_printf("%Zd\n",rop);
     mpz_powm_ui(rop,i5,d,n);
      gmp_printf("%Zd\n",rop);
      mpz_powm_ui(rop,i6,d,n);
      gmp_printf("%Zd\n",rop);

     /* if(rop!=i){
      cout<<"wrong"<<endl;
      
      
  }*/
  return 0;
}
