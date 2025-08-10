package io.github.some_example_name;

public class Matrix {
    private float[] matrix;

    public Matrix(float[] matrix) {
        this.matrix = matrix;
    }

    public void setMatrixSection(int i, float value) {
        matrix[i] = value;
    }
    public float getMatrixSection(int i) {
        return matrix[i];
    }
}
