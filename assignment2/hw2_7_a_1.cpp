#include <iostream>
#include <math.h>
#include <gmp.h>

using namespace std;

int main(){
        int i=1,j,q=96737;
	mpz_t p,count,rop;
	mpz_init(p);
        mpz_init(count);
	mpz_init(rop);
	mpz_init_set_str(p,"96737",10);
	mpz_init_set_str(count,"1",10);
	while(i++){
            for(int j=1;j<q;j++){
		    mpz_ui_pow_ui(rop,i,j);
		    mpz_powm(rop,rop,count,p);
	    if (rop==count&&j==96736){
	         cout<<i<<endl;
		 break;
	       }
	    }
	}
	return 0;

}
