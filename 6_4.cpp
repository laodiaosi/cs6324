#include <iostream>
#include <math.h>

using namespace std;

int main(){
   long double ciphertext[6]={10000,20000,30000,40000,50000,60000};
  long double d=1952050;
   long double n=60529591;
   long double c;
  for(int i=0;i<=5;i++){
    c=fmod(pow(ciphertext[i],d),n);
    c=pow(ciphertext[i],d);
    cout<<c<<endl;
  }
  return 0;
}
