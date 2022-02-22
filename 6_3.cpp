#include <iostream>
#include <math.h>
#include <gmp.h>

using namespace std;

int main(){
  int i;
  int N=60529591;
  int j;
  int e=31;
  string c;
  string m;
  int d=1952050;
  //ed=29280751*31;
  for( i=1;i<5;i++){
        c=getCountExp(i,e);
        cout<<c<<endl;

  }
  return 0;
}

