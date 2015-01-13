extern "C"
__global__ void kMul(double* a, double* b, double* dest, int n) {
  int idx = blockIdx.x * blockDim.x + threadIdx.x;
  if(idx<n) {
    dest[idx] = a[idx] * b[idx];
  }
}


extern "C"
__global__ void kFillArray(double* a, int m, double* dest, int n) {
  int idx = blockIdx.x * blockDim.x + threadIdx.x;
  if(idx<n) {
    dest[idx] = a[idx % m];
  }
}


extern "C"
__global__ void kFill(double v, double* dest, int n) {
  int idx = blockIdx.x * blockDim.x + threadIdx.x;
  if(idx<n) {
    dest[idx] = v;
  }
}

extern "C"
__global__ void kSigmoid(double* a, double* dest, int n) {
  int idx = blockIdx.x * blockDim.x + threadIdx.x;
  if(idx<n) {
    dest[idx] = 1/(1+__expf(-1*a[idx]));
  }
}
