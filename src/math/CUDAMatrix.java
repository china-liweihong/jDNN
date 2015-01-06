package math;

import math.jcublas.SimpleCuBlas;
import random.RandomUtils;
import java.lang. AutoCloseable;
import jcuda.jcublas.JCublas;
import jcuda.Pointer;

public class CUDAMatrix extends DMatrix implements AutoCloseable {
  public CUDAMatrix(int r, int c) {
    super(r, c);
    this.persist = false;
  }

  public CUDAMatrix(int r, int c, double[] d) {
    super(r, c, d);
    this.persist = false;
  }

  public CUDAMatrix(int r, int c, boolean _persist) {
    super(r, c);
    this.persist = _persist;
    if(this.persist) {
      this.cPointer = SimpleCuBlas.alloc(new double[r*c]);
    }
  }

  public CUDAMatrix(int r, int c, double[] d, boolean _persist) {
    super(r, c, d);
    this.persist = _persist;
    if(this.persist) {
      this.cPointer = SimpleCuBlas.alloc(d);
    }
  }

  public void close() {
//    System.err.printf("close() in CUDAMatrix\n");
    if(this.cPointer != null) {
//      System.out.printf("Releasing the CUDA Pointer\n");
      this.persist = false;
      JCublas.cublasFree(this.cPointer);
      this.cPointer = null;
    }
  }

  protected void finalize() {
//    System.err.printf("finalize() in CUDAMatrix()\n");
    if(this.cPointer != null) {
      this.close();
    }
  }

  public void copyHtoD() {
    if(System.getProperty("use_cuda").equals("true")) {
      if(this.persist == false)
        this.persist = true;
      if(this.cPointer != null) {
        JCublas.cublasFree(this.cPointer);
        this.cPointer = null;
      }
   
      this.cPointer = SimpleCuBlas.alloc(this.data());
    }
  }

  public void copyDtoH() {
    if(System.getProperty("use_cuda").equals("true")) {
      if (this.cPointer!=null) {
        SimpleCuBlas.getData(this,this.cPointer,Pointer.to(this.data()));
      }
    }
  }

  public void updateDeviceData() {
    if(this.cPointer!=null)
      SimpleCuBlas.updateData(this.cPointer, this.data);
  }
  
  public void updateDeviceData(double[] newData) {
    if(this.cPointer!=null)
      SimpleCuBlas.updateData(this.cPointer, newData);
  }
  
  public static DMatrix zeros(int r, int c) {
    return new CUDAMatrix(r, c);
  }

  public static DMatrix zeros(int r, int c, boolean _persist) {
    return new CUDAMatrix(r, c, _persist);
  }

  public static DMatrix ones(int r, int c) {
    DMatrix m = new CUDAMatrix(r, c);
    for(int i=0; i<r*c; i++)
      m.put(i, 1.0);
    return m;
  }

  public static DMatrix ones(int r, int c, boolean _persist) {
    DMatrix m = new CUDAMatrix(r, c);
    m.persist = _persist;
    for(int i=0; i<r*c; i++)
      m.put(i, 1.0);
    if(m.persist) {
      m.cPointer = SimpleCuBlas.alloc(m.data());
    }
    return m;
  }
  
  public static DMatrix randn(int r, int c) {
    DMatrix m = new CUDAMatrix(r, c);
    for (int i = 0; i < r * c; i++)
      m.put(i, RandomUtils.nextGaussian());
    return m;
  }
  
  public static DMatrix randn(int r, int c, boolean _persist) {
    DMatrix m = new CUDAMatrix(r, c);
    m.persist = _persist;
    for (int i = 0; i < r * c; i++)
      m.put(i, RandomUtils.nextGaussian());
    if(m.persist) {
      m.cPointer = SimpleCuBlas.alloc(m.data());
    }
    return m;
  }
  
  public DMatrix transpose() {
    return new CUDAMatrix(this.columns, this.rows, this.data);
  }
 

  // y = Ax+y
  public DMatrix addMuli(DMatrix A, DMatrix x) {
    SimpleCuBlas.gemv(A, x, this, 1.0, 1.0);
    return this;
  }



  // y = 1*x+y
  public DMatrix add(DMatrix other) {
    assert (this.length()==other.length());
    DMatrix m = new CUDAMatrix(this.rows, this.columns, this.data());
    SimpleCuBlas.axpy(1.0, other, m);
    return m;
  }
  public DMatrix addi(DMatrix other) {
    assert (this.length()==other.length());
//    System.out.printf("Using cuda blas\n");
    SimpleCuBlas.axpy(1.0, other, this);
    return this;
  }

  // y = a*X+b
  public DMatrix addi(double a, DMatrix other) {
    SimpleCuBlas.axpy(a, other, this);
    return this;
  }

  public DMatrix add(double v) {
    DMatrix m = DMath.createMatrix(this.rows(), this.columns(), this.toArray());
    for (int i = 0; i < this.length(); i++)
      m.put(i,(double) v+m.get(i));
    return m;
  }
  public DMatrix addi(double v) {
    for (int i = 0; i < this.length(); i++)
      this.put(i,(double) v+this.get(i));
    return this;
  }
  
  public DMatrix sub(DMatrix other) {
    assert (this.length()==other.length());
    DMatrix m = new CUDAMatrix(this.rows(), this.columns(), this.toArray());
    SimpleCuBlas.axpy(-1.0, other, m);
    return m;
  }

  public DMatrix subi(DMatrix other) {
    assert (this.length()==other.length());
    SimpleCuBlas.axpy(-1.0, other, this);
    return this;
  }
  
  public DMatrix mul(DMatrix other) {
    assert (this.length()==other.length());
    DMatrix m = new CUDAMatrix(this.rows(), this.columns());
    SimpleCuBlas.mul(this, other, m);
    return m;
  }

  public DMatrix muli(DMatrix other) {
    assert (this.length()==other.length());
    SimpleCuBlas.mul(this, other, this);
    return this;
  }

  public DMatrix mul(double v) {
    DMatrix m = new CUDAMatrix(this.rows(), this.columns(), this.toArray());
    SimpleCuBlas.scal(m, v);
    return m;
  }
  public DMatrix muli(double v) {
    SimpleCuBlas.scal(this, v);
    return this;
  }

  public DMatrix mmul(DMatrix other) {
    assert (this.columns()==other.rows());
    DMatrix m = new CUDAMatrix(this.rows(), this.columns());
//    SimpleCuBlas.gemm(other, this, m, 1.0, 0.0);
    return mmuli(other, m);
//    return m;
  }

  //result = this*other
  public DMatrix mmuli(DMatrix other, DMatrix result) {
    assert (this.columns()==other.rows());
    if (result.rows != rows || result.columns != other.columns) {
      if (result != this && result != other) {
        result.resize(this.rows, other.columns);
      } else {
        System.err.printf("Cannot resize result matrix because it is used in-place.\n\n");
      }
    }

    if (result == this || result == other) {
      /* actually, blas cannot do multiplications in-place. Therefore, we will fake by
       * * allocating a temporary object on the side and copy the result later.
       * */
      DMatrix temp = new CUDAMatrix(result.rows(), result.columns());
      if (other.columns == 1) {
        SimpleCuBlas.gemv(this, other, temp, 1.0, 0.0);
      } else {
        SimpleCuBlas.gemm(this, other, temp, 1.0, 0.0);
      }
      SimpleCuBlas.copy(temp, result);
    } 
    else {
      if (other.columns == 1) {
        SimpleCuBlas.gemv(this, other, result, 1.0, 0.0);
      } else {
        SimpleCuBlas.gemm(this, other, result, 1.0, 0.0);
      }
    }

    return result;
  }
  public DMatrix mmuli(DMatrix other) {
    return mmuli(other, this);
  }

}