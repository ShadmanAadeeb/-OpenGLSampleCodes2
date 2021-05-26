package com.example.opengldrawingbasics2;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cube {
    //floating point arrays
    float[] vertices;
    short[] indices;
    float[] vertexColors;
    //matrices
    float[] projectionMatrix = new float[16];
    float[] viewMatrix = new float[16];
    float[] productMatrix = new float[16];
    //Buffers
    FloatBuffer vertexBuffer;
    ShortBuffer indexBuffer;
    FloatBuffer colorBuffer;
    //Shading Program Strings
    String vertexShaderCodeString;
    String fragmentShaderCodeString;
    //Handle to the shader programs
    int vertexShaderHandle;
    int fragmentShaderHandle;
    //Handle to the  program
    int programHandle;
    //The variable handles
    int mPositionHandle;
    int mMVPMatrixHandle;
    int mColorHandle;

    //Rotation matrix
    float[] mRotationMatrix=new float[16];
    Matrix rotationMatrix;

    public Cube(){
        //*******************INITIALIZING THE ARRAYS START*******************//
        //Initialize the Vertices at first
        vertices= new float[]{
                0,0,0,//v0
                1,0,0,//v1
                1,1,0,//v2
                0,1,0,//v3
                0,0,1,//v4
                1,0,1,
                1,1,1,
                0,1,1
        };
        indices=new short[]{
           0,1,2,2,3,0, //face1
           0,4,7,7,3,4,
           1,5,6,5,6,2,
           4,5,6,5,6,7,
           3,2,6,2,6,7,
           1,5,4,5,4,0
        };
        vertexColors=new float[]{
                1,0,0,
                1,1,1,
                1,1,1,
                1,1,1,
                1,1,1,
                1,1,1,
                1,1,1,
                1,1,1
        };
        //*******************INITIALIZING THE ARRAYS ENDS*******************//

        //******SETTING UP THE PROJECTION MATRIX STARTS(along with rotation matrix)**********//
        Matrix.frustumM(projectionMatrix, 0,
                -1, 1,
                -1, 1,
                2, 9);
        Matrix.setLookAtM(viewMatrix, 0,
                0, 3, -4,
                0, 0, 0,
                0, 1, 0);


        Matrix.setRotateM(mRotationMatrix,0,30,0,0,1);

        Matrix.multiplyMM(viewMatrix,0,viewMatrix,0,mRotationMatrix,0);

        Matrix.multiplyMM(productMatrix, 0,
                projectionMatrix, 0,
                viewMatrix, 0);


        //******************SETTING UP THE PROJECTION MATRIX ENDS*****************//

        //********************PREPARING THE BUFFERS STARTS*********************//
        //******Going to make a vertex buffer placing the co-ordinates starts**********//
        //It is going to store the point values in float size spaces and hence making spaces
        ByteBuffer byteBuffer=ByteBuffer.allocateDirect(vertices.length*4);
        //Ordering the contents of the byte buffer
        byteBuffer.order(ByteOrder.nativeOrder());
        //Turning the vertex buffer into a float buffer
        vertexBuffer =byteBuffer.asFloatBuffer();
        //Placing the floating point array in the vertex buffer
        vertexBuffer.put(vertices);
        //Repositioning the buffer contents
        vertexBuffer.position(0);
        //******Going to make a vertex buffer placing the co-ordinates ends**********//

        //******Going to make a vertex buffer for placing the color starts**********//
        //It is going to store the point values in float size spaces and hence making spaces
        ByteBuffer byteBuffer2=ByteBuffer.allocateDirect(indices.length*2);
        //Ordering the contents of the byte buffer
        byteBuffer2.order(ByteOrder.nativeOrder());
        //Turning the vertex buffer into a float buffer
        indexBuffer =byteBuffer2.asShortBuffer();
        //Placing the floating point array in the vertex buffer
        indexBuffer.put(indices);
        //Repositioning the buffer contents
        indexBuffer.position(0);
        //******Going to make a vertex buffer for placing the color ends**********//

        //******Going to make a vertex buffer for placing the color starts**********//
        //It is going to store the point values in float size spaces and hence making spaces
        ByteBuffer byteBuffer3=ByteBuffer.allocateDirect(vertexColors.length*4);
        //Ordering the contents of the byte buffer
        byteBuffer3.order(ByteOrder.nativeOrder());
        //Turning the vertex buffer into a float buffer
        colorBuffer =byteBuffer3.asFloatBuffer();
        //Placing the floating point array in the vertex buffer
        colorBuffer.put(vertexColors);
        //Repositioning the buffer contents
        colorBuffer.position(0);
        //******Going to make a vertex buffer for placing the color ends**********//
        //********************PREPARING THE BUFFERS ENDS*********************//

        //********************PREPARING THE SHADING PROGRAMS STARTS********************//
        vertexShaderCodeString =
                "attribute vec3 aVertexPosition;"+  //vector variable for holding vertices
                        "uniform mat4 uMVPMatrix;"+ //matrix for multiplying the vertices
                        "attribute vec4 vertexColorFromMainProgram;"+ //variable for holding the color
                        "varying vec4 vColor;" + //variable for passing colors to fragment shader
                        "void main() {"+
                        "gl_Position = uMVPMatrix *vec4(aVertexPosition,1.0);" +//points placed for drawing
                        "vColor=vertexColorFromMainProgram;}";//passing the colors to fragment shader
        fragmentShaderCodeString =
                "precision mediump float;"+//setting precision for floating point variables
                        "varying vec4 vColor; "+//this will hold the colors passed to it from vertex shader
                        "void main() {"+
                        "gl_FragColor = vColor;}";//colors placed for drawing
        //********************PREPARING THE SHADING PROGRAMS ENDS********************//

        //************************COMPILING THE SHADER PROGRAMS STARTS*********************//
        //Also I am obtaining handler to the shader programs
        vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderHandle, vertexShaderCodeString);

        fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCodeString);

        GLES20.glCompileShader(vertexShaderHandle);
        GLES20.glCompileShader(fragmentShaderHandle);
        //************************COMPILING THE SHADER PROGRAMS ENDS*********************//

        //********CREATING THE MAIN PROGRAM AND ATTACHING IT WITH SHADERS STARTS****//
        programHandle=GLES20.glCreateProgram();
        //ATTACHING
        GLES20.glAttachShader(programHandle,vertexShaderHandle);
        GLES20.glAttachShader(programHandle,fragmentShaderHandle);
        //LINKING
        GLES20.glLinkProgram(programHandle);
        GLES20.glUseProgram(programHandle);
        //********CREATING THE MAIN PROGRAM AND ATTACHING IT WITH SHADERS ENDS****//

        //**********GETTING HANDLE TO THE VARIABLES STARTS*************//
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "aVertexPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
        //Enable the handle
        GLES20.glEnableVertexAttribArray(mMVPMatrixHandle);
        //get handle to the color variable
        mColorHandle= GLES20.glGetAttribLocation(programHandle, "vertexColorFromMainProgram");
        //Enable the handle
        GLES20.glEnableVertexAttribArray(mColorHandle);
        //**********GETTING HANDLE TO THE VARIABLES STARTS*************//
        Log.d("TAG1","CAME HERE");
    }

    //Each vertex has 3 co-ordinates x,y,z
    int CO_ORDS_PER_VERTEX =3;
    //(x1,y1,z1,x2,y2,z2)<- for moving pointer from x1 to x2 3X4=12 bytes are to be moved
    int vertexSizeInBytes =12; //also vertex stride
    //Each color has 3 values RGB
    int VALUES_PER_COLOR =3;
    //(r1,g1,b1,r2,g2,b2)<- for moving pointer from x1 to x2 3X4=12 bytes are to be moved
    int colorSizeInBytes=12;  //also color stride

    public void draw() {

        GLES20.glUseProgram(programHandle);//use the object's shading programs
        // Providing value to shader matrix
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, productMatrix, 0);
        // Providing value to shader position
        GLES20.glVertexAttribPointer(mPositionHandle, CO_ORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexSizeInBytes, vertexBuffer);
        //Providing value to shader color
        GLES20.glVertexAttribPointer(mColorHandle, VALUES_PER_COLOR,
                GLES20.GL_FLOAT, false, colorSizeInBytes, colorBuffer);
        // Drawing the shape using the index buffer
        Log.d("TAG1","CAME HERE 2");
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                indices.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        Log.d("TAG1","CAME HERE 3");
    }
}
