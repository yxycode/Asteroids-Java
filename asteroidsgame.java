
import javax.swing.*;
import java.awt.*;
    
import java.awt.event.*;
import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.awt.image.BufferedImage;

import javax.sound.sampled.*;
import java.io.File;

//###############################################################
abstract class GameObject
{
    public static World ItsWorld;
    public static int ScreenWidth = 800;
    public static int ScreenHeight = 600;
    public static final int SCREEN_BORDER_THICKNESS = 100;
    
    public static int KeyPressDelayCount = 0;
    public static int KeyPressDelayCountMax = 2;    
    
    public static final float pi = 3.141594f;
    public static final Random Rand = new Random();
    protected float x, y;
    protected float Angle;
    public int ActiveFlag = 1;
    
    public static int vk_up = 0, vk_down = 0, vk_left = 0, vk_right = 0, vk_r = 0, vk_f = 0;
    
    public final static int ID_NOTHING = 0, ID_ASTEROID = 1, ID_SHIP = 2, ID_EXPLODING_SHIP = 4, 
        ID_PROJECTILE = 5, ID_INFORMATION_BAR = 6;
    public int Id;
    
    public void Do() {};
    public void Draw( Graphics g ) {};
    protected void DrawOvalCenter( Graphics g, int x, int y, int width, int height )
    {
       g.drawOval( x - width/2, y - height/2, width, height );
    }
};        
//###############################################################
class Asteroid extends GameObject
{
   public static final int MAX_VERTEX_COUNT = 10;
   public static final int MAX_BREAK_UP_PIECES = 6;
   
   protected float[] vertex_list_polar_distance;
   protected float[] vertex_list_polar_angle;
   protected int[] vertex_list_x;
   protected int[] vertex_list_y;
   protected float x_center, y_center;
   protected float rotate_increment = 2;
   protected int Radius, MoveAngle, Speed = 2;

//---------------------------------------------------------------   
   public Asteroid( int radius )
   {
      int i; float angle; int distance;
      int min_radius, max_radius;
      
      Id = ID_ASTEROID;
      
      Radius = radius;
      min_radius = (int)(0.75 * Radius);
      max_radius = Radius;
      
      MoveAngle = Rand.nextInt(360);
      
      vertex_list_polar_distance = new float[MAX_VERTEX_COUNT];
      vertex_list_polar_angle = new float[MAX_VERTEX_COUNT];
      vertex_list_x = new int[MAX_VERTEX_COUNT];
      vertex_list_y = new int[MAX_VERTEX_COUNT]; 
      
      x_center = Rand.nextInt(ScreenWidth);
      y_center = Rand.nextInt(ScreenHeight);
      
      if( Rand.nextInt(2) == 1 )
         rotate_increment = 2;
      else
         rotate_increment = -2;
      
      angle = 0;
      for( i = 0; i < MAX_VERTEX_COUNT; i++ )
      {
        angle += 360 / MAX_VERTEX_COUNT;
        distance = Rand.nextInt(max_radius + 1 - min_radius) + min_radius;
        vertex_list_polar_angle[i] = angle;
        vertex_list_polar_distance[i] = distance;
        
        vertex_list_x[i] = (int)(Math.cos(pi/180f * angle) * distance + x_center);
        vertex_list_y[i] = (int)(Math.sin(pi/180f * angle) * distance + y_center);            
      }
          
   }
//---------------------------------------------------------------   
   protected void Rotate()
   {
     int i;
     
      for( i = 0; i < MAX_VERTEX_COUNT; i++ )
      {
        vertex_list_polar_angle[i] += rotate_increment;
        if( vertex_list_polar_angle[i] < 0 )
            vertex_list_polar_angle[i] = 360 + vertex_list_polar_angle[i];
        else
        if( vertex_list_polar_angle[i] > 360 )
            vertex_list_polar_angle[i] -= 360;       
            
        vertex_list_x[i] = (int)(Math.cos(pi/180f * vertex_list_polar_angle[i]) 
            * vertex_list_polar_distance[i] + x_center);
        vertex_list_y[i] = (int)(Math.sin(pi/180f * vertex_list_polar_angle[i]) 
            * vertex_list_polar_distance[i] + y_center);  
      }
   }
//---------------------------------------------------------------   
   public void Move()
   {
      x_center = (float)(Math.cos(pi/180f * MoveAngle) * Speed + x_center);
      y_center = (float)(Math.sin(pi/180f * MoveAngle) * Speed + y_center); 

      if( x_center < -SCREEN_BORDER_THICKNESS )
          x_center = ScreenWidth + SCREEN_BORDER_THICKNESS;    
      if( y_center < -SCREEN_BORDER_THICKNESS )
          y_center = ScreenHeight + SCREEN_BORDER_THICKNESS; 
      if( x_center > ScreenWidth + SCREEN_BORDER_THICKNESS )
          x_center = -SCREEN_BORDER_THICKNESS;    
      if( y_center > ScreenHeight +SCREEN_BORDER_THICKNESS )
          y_center = -SCREEN_BORDER_THICKNESS;            
   }
//---------------------------------------------------------------   
   public void SetXY( float x0, float y0 )
   {
     int i;
     
     x_center = x0; y_center = y0;
     
      for( i = 0; i < MAX_VERTEX_COUNT; i++ )
      {
        vertex_list_x[i] = (int)(Math.cos(pi/180f * vertex_list_polar_angle[i]) 
            * vertex_list_polar_distance[i] + x_center);
        vertex_list_y[i] = (int)(Math.sin(pi/180f * vertex_list_polar_angle[i]) 
            * vertex_list_polar_distance[i] + y_center);  
      }   
   }
//---------------------------------------------------------------   
   public Asteroid[] Explode()
   {
      ActiveFlag = 0;
      int divided_radius = (int)(Radius/2.0f);
      int i;
      float x0, y0, angle0;
      
      Asteroid[] AsteroidPiecesList = new Asteroid[MAX_BREAK_UP_PIECES];
      
      if( divided_radius > 10 )
      {
        int piece_count =  Rand.nextInt(5) + 2;
                
        angle0 = pi/180f * Rand.nextInt(360);
        
        for( i = 0; i < piece_count; i++ )
        { 
          AsteroidPiecesList[i] = new Asteroid(divided_radius);         
          x0 = (float)(Math.cos(angle0) * Rand.nextInt(Radius) + x_center);
          y0 = (float)(Math.sin(angle0) * Rand.nextInt(Radius) + y_center);
          AsteroidPiecesList[i].SetXY( x0, y0 );
        }
        return AsteroidPiecesList;
      }
      
      return null;
   }
//---------------------------------------------------------------   
   public void Explode( GameObject[] obj_list )
   {
     Asteroid[] AsteroidPiecesList = Explode();
     int i, k;
     
     if( AsteroidPiecesList == null )
         return;
     
     k = 0;
     for( i = 0; i < obj_list.length; i++ )
       if( obj_list[i] == null )
       {
         obj_list[i] = AsteroidPiecesList[k];
         k++;
         if( k > AsteroidPiecesList.length - 1 )
             break;
       }
   }
//---------------------------------------------------------------
   public int[] GetXYRadius()
   {
     int[] xyr = new int[3];
     
     xyr[0] = (int)x_center; xyr[1] = (int)y_center; xyr[2] = (int)Radius;
     
     return xyr;
   }   
//---------------------------------------------------------------
   public void Do()
   {
      Rotate();
      Move();
   }
//---------------------------------------------------------------   
   public void Draw( Graphics g ) 
   {
      g.drawPolygon( vertex_list_x, vertex_list_y, MAX_VERTEX_COUNT);
   }  
//---------------------------------------------------------------   
} 
//###############################################################
class Ship extends GameObject
{

   public static final int MAX_VERTEX_COUNT = 3;
   public static final int MAX_VECTORS = 36;
   public static final int VECTOR_ANGLE_INCREMENT = 360/MAX_VECTORS;
      
   protected float[] vertex_list_polar_distance;
   protected float[] vertex_list_polar_angle;
   protected int ShipProwAngle;
   protected int[] vertex_list_x;
   protected int[] vertex_list_y;
   protected float x_center, y_center;
   protected int Radius;
   protected int Diameter;
   
   public static final float MAX_VECTOR_SPEED = 10f;
   public static final float VECTOR_ACCELERATION = 0.2f;
   protected float[] vector_speed;    
   
   protected static final int GOING_WARP_COUNTER_MAX = 20;
   protected int GoingWarpCounter = GOING_WARP_COUNTER_MAX;
   protected int WarpFlag = 0;

//---------------------------------------------------------------   
public Ship()
{
  Id = ID_SHIP;
  
  x_center = ScreenWidth/2;
  y_center = ScreenHeight/2;
  
  Radius = 15;
  Diameter = Radius * 2;
  
      vertex_list_polar_distance = new float[MAX_VERTEX_COUNT];
      vertex_list_polar_angle = new float[MAX_VERTEX_COUNT];
      vertex_list_x = new int[MAX_VERTEX_COUNT];
      vertex_list_y = new int[MAX_VERTEX_COUNT];   
      
      vector_speed = new float[MAX_VECTORS];
      
  vertex_list_polar_distance[0] = 15;
  vertex_list_polar_angle[0] = 90;
  vertex_list_polar_distance[1] = 15;
  vertex_list_polar_angle[1] = 180 + 45;
  vertex_list_polar_distance[2] = 15;
  vertex_list_polar_angle[2] = 270 + 45;
  
  ShipProwAngle = 90;
  
  int i; float angle, distance;
  
      for( i = 0; i < MAX_VERTEX_COUNT; i++ )
      {
        angle = vertex_list_polar_angle[i];
        distance = vertex_list_polar_distance[i];
        vertex_list_x[i] = (int)(Math.cos(pi/180f * angle) * distance + x_center);
        vertex_list_y[i] = (int)(Math.sin(pi/180f * angle) * distance + y_center);            
      }  
      
}
//---------------------------------------------------------------
protected void Rotate( int rotate_increment )
{
     int i;
    
      for( i = 0; i < MAX_VERTEX_COUNT; i++ )
      {
        vertex_list_polar_angle[i] += rotate_increment;
        if( vertex_list_polar_angle[i] < 0 )
            vertex_list_polar_angle[i] = 360 + vertex_list_polar_angle[i];
        else
        if( vertex_list_polar_angle[i] > 360 )
            vertex_list_polar_angle[i] -= 360;       
            
        vertex_list_x[i] = (int)(Math.cos(pi/180f * vertex_list_polar_angle[i]) 
            * vertex_list_polar_distance[i] + x_center);
        vertex_list_y[i] = (int)(Math.sin(pi/180f * vertex_list_polar_angle[i]) 
            * vertex_list_polar_distance[i] + y_center);  
      }
      
      ShipProwAngle += Math.signum(rotate_increment) * 8;
      
      if( ShipProwAngle < 0 )
          ShipProwAngle = 360 + ShipProwAngle;
      if( ShipProwAngle > 360 )
          ShipProwAngle -= 360;          
}
//---------------------------------------------------------------
public void Accelerate( int ForwardBackwardSign )
{
      int index;
      index = ShipProwAngle / VECTOR_ANGLE_INCREMENT;
      
      vector_speed[index] += VECTOR_ACCELERATION * ForwardBackwardSign;
      
      if( vector_speed[index] > MAX_VECTOR_SPEED )
          vector_speed[index] = MAX_VECTOR_SPEED;
}
//---------------------------------------------------------------
public void UpdateXY()
{
   int i;
      if( x_center < -SCREEN_BORDER_THICKNESS )
          x_center = ScreenWidth + SCREEN_BORDER_THICKNESS;    
      if( y_center < -SCREEN_BORDER_THICKNESS )
          y_center = ScreenHeight + SCREEN_BORDER_THICKNESS; 
      if( x_center > ScreenWidth + SCREEN_BORDER_THICKNESS )
          x_center = -SCREEN_BORDER_THICKNESS;    
      if( y_center > ScreenHeight +SCREEN_BORDER_THICKNESS )
          y_center = -SCREEN_BORDER_THICKNESS;    
     
      for( i = 0; i < MAX_VERTEX_COUNT; i++ )
      {
        vertex_list_x[i] = (int)(Math.cos(pi/180f * vertex_list_polar_angle[i]) 
            * vertex_list_polar_distance[i] + x_center);
        vertex_list_y[i] = (int)(Math.sin(pi/180f * vertex_list_polar_angle[i]) 
            * vertex_list_polar_distance[i] + y_center);  
      }   
}
//---------------------------------------------------------------
public void AddMovementVectors()
{
  int i; float xv, yv;
  for( i = 0; i < MAX_VECTORS; i++ )
  {
    xv = (float)(Math.cos(pi/180f * i * VECTOR_ANGLE_INCREMENT) * vector_speed[i]);
    yv = (float)(Math.sin(pi/180f * i * VECTOR_ANGLE_INCREMENT) * vector_speed[i]);
    
    x_center += xv; y_center += yv;
  }
  UpdateXY();
}
//---------------------------------------------------------------
public void Do()
{

 if( KeyPressDelayCount >= KeyPressDelayCountMax &&
     WarpFlag != 1 )
 {
   if( vk_left == 1 )
   {
       Rotate( -8 );       
       KeyPressDelayCount = 0; 
   } 
   else
   if( vk_right == 1 )
   {  
       Rotate( 8 );
       KeyPressDelayCount = 0;  
   }
   
   if( vk_up == 1 )
   {
     Accelerate(1);
     KeyPressDelayCount = 0;  
   }
   else
   if( vk_down == 1 )
   {
     Accelerate(-1);
     KeyPressDelayCount = 0;  
   }   
   
   if( vk_f == 1 )
   {
     if( Projectile.SpeciesCount < Projectile.MAX_SPECIES_COUNT )
     {
        ItsWorld.AddGameObject( new Projectile( x_center, y_center, ShipProwAngle ));
        SoundBox.me.Play(0);
     }
     KeyPressDelayCount = 0;  
   }  
   if( vk_r == 1 )
   {
      WarpFlag = 1;
      KeyPressDelayCount = 0;
      SoundBox.me.Play(3);
   }
 }
 
 if( WarpFlag == 1 )
 {
   GoingWarpCounter--;
   
   if( GoingWarpCounter <= 0 )
   {
      GoingWarpCounter = GOING_WARP_COUNTER_MAX;
      WarpFlag = 0;
      
      x_center = Rand.nextInt(ScreenWidth);
      y_center = Rand.nextInt(ScreenHeight);
      UpdateXY();      
   }
 }
 
 AddMovementVectors();
 CheckCollideAsteroid();
}
//---------------------------------------------------------------
protected void Destroy()
{
   ActiveFlag = 0;
}
//---------------------------------------------------------------
public void CheckCollideAsteroid()
{
  int i;
  float xdelta, ydelta, radius, distance;
  int[] XYRadius;
  GameObject[] GameObjectList = ItsWorld.GetGameObjectList();
  Asteroid ast_obj;
  
    for( i = 0; i < World.MAX_GAME_OBJECT_COUNT; i++ )
      if( GameObjectList[i] != null )
       if( GameObjectList[i].ActiveFlag > 0 ) 
        if( GameObjectList[i].Id == ID_ASTEROID )
       {
          ast_obj = (Asteroid)GameObjectList[i];
          
          XYRadius = ast_obj.GetXYRadius();
          
          xdelta = XYRadius[0] - x_center;
          ydelta = XYRadius[1] - y_center;
          radius = XYRadius[2];
          distance = (float)Math.sqrt( xdelta * xdelta + ydelta * ydelta );
          if( distance < radius )
          {
             ItsWorld.AddGameObject( new ExplodingShip( x_center, y_center, Radius ));
             Destroy();
             break;
          }
          
       }
}
//---------------------------------------------------------------
public void Draw( Graphics g )
{
   g.drawPolygon( vertex_list_x, vertex_list_y, MAX_VERTEX_COUNT);
   
   if( WarpFlag == 1 )
       DrawOvalCenter( g, (int)x_center, (int)y_center, Diameter, Diameter);   
}
//---------------------------------------------------------------
}
//###############################################################
class ExplodingShip extends GameObject
{

   public static final int MAX_VERTEX_COUNT = 3;
   public static final int MAX_LINE_COUNT = 3;
      
   protected int[] vertex_list_x;
   protected int[] vertex_list_y;   
   protected int[] line_point_list_x;
   protected int[] line_point_list_y;
   protected int[] MoveAngle;
   protected float x_center, y_center;
    
   public final static int EXPLODING_SPEED = 3;
   public final static int EXPLODING_COUNTER_MAX = 100;
   public int ExplodingCounter = EXPLODING_COUNTER_MAX;
   
//---------------------------------------------------------------   
public ExplodingShip( float x_start, float y_start, int Radius )
{
  int i; float angle, distance;
  
  Id = ID_EXPLODING_SHIP;
  
  vertex_list_x = new int[MAX_VERTEX_COUNT];
  vertex_list_y = new int[MAX_VERTEX_COUNT];
  line_point_list_x = new int[MAX_LINE_COUNT * 2]; 
  line_point_list_y = new int[MAX_LINE_COUNT * 2]; 
  MoveAngle = new int[MAX_VERTEX_COUNT];
   
  for( i = 0; i < MAX_VERTEX_COUNT; i++ )
  {
    angle = Rand.nextInt(360);
    MoveAngle[i] = Rand.nextInt(360);
    vertex_list_x[i] = (int)(Math.cos( pi/180f * angle ) * Radius + x_start);
    vertex_list_y[i] = (int)(Math.sin( pi/180f * angle ) * Radius + y_start);    
  }  

  line_point_list_x[0] = vertex_list_x[0];
  line_point_list_y[0] = vertex_list_y[0];
  line_point_list_x[1] = vertex_list_x[1];
  line_point_list_y[1] = vertex_list_y[1]; 

  line_point_list_x[2] = vertex_list_x[1];
  line_point_list_y[2] = vertex_list_y[1];
  line_point_list_x[3] = vertex_list_x[2];
  line_point_list_y[3] = vertex_list_y[2]; 

  line_point_list_x[4] = vertex_list_x[2];
  line_point_list_y[4] = vertex_list_y[2];
  line_point_list_x[5] = vertex_list_x[0];
  line_point_list_y[5] = vertex_list_y[0];   

  SoundBox.me.Play(2); 
}
//---------------------------------------------------------------
protected void Move()
{
   int i, xdelta, ydelta;
   for( i = 0; i < MAX_LINE_COUNT; i++ )
   {
     xdelta = (int)(Math.cos( pi/180f * MoveAngle[i] ) * EXPLODING_SPEED );
     ydelta = (int)(Math.sin( pi/180f * MoveAngle[i] ) * EXPLODING_SPEED );
     line_point_list_x[i * 2] += xdelta;
     line_point_list_y[i * 2] += ydelta;    
     line_point_list_x[i * 2 + 1] += xdelta;
     line_point_list_y[i * 2 + 1] += ydelta;      
   }
}
//---------------------------------------------------------------
public void Do()
{
   Move();
   
   ExplodingCounter--;
   
   if( ExplodingCounter <= 0 )
   {
     ActiveFlag = 0;    
     
     if( InformationBar.me.Lives > 0 )
     {
       InformationBar.me.Lives--;

       if( InformationBar.me.Lives >= 1 )
           World.me.AddGameObject( new Ship() );
     }
   }
}
//---------------------------------------------------------------
public void Draw( Graphics g )
{
   int i;
   
   for( i = 0; i < MAX_LINE_COUNT; i++ )
   {
      g.drawLine( line_point_list_x[i * 2], line_point_list_y[i * 2],
        line_point_list_x[i * 2 + 1], line_point_list_y[i * 2 + 1] );
   }
}
//---------------------------------------------------------------
}
//###############################################################
class Projectile extends GameObject
{
  protected final static int MAX_SPECIES_COUNT = 5;
  public static int SpeciesCount = 0;
  protected static int MAX_LIFE_SPAN_FRAMES = 50;
  protected final static int MAX_SPEED = 6;
  protected final static int DIAMETER = 10;

  protected int LifeSpanCounter, MoveAngle;
  protected float x_center, y_center;
  
//---------------------------------------------------------------  
public Projectile( float x_start, float y_start, int move_angle )
{
  Id = ID_PROJECTILE;
  LifeSpanCounter = 0;
  SpeciesCount++;
  x_center = x_start; y_center = y_start; MoveAngle = move_angle;
}
//---------------------------------------------------------------
public void Destroy()
{
  if( SpeciesCount > 0 )
      SpeciesCount--;
  ActiveFlag = 0;
}
//---------------------------------------------------------------
public void CheckCollideAsteroid()
{
  int i;
  float xdelta, ydelta, radius, distance;
  int[] XYRadius;
  GameObject[] GameObjectList = ItsWorld.GetGameObjectList();
  Asteroid ast_obj;
  
    for( i = 0; i < World.MAX_GAME_OBJECT_COUNT; i++ )
      if( GameObjectList[i] != null )
       if( GameObjectList[i].ActiveFlag > 0 ) 
        if( GameObjectList[i].Id == ID_ASTEROID )
       {
          ast_obj = (Asteroid)GameObjectList[i];
          
          XYRadius = ast_obj.GetXYRadius();
          
          xdelta = XYRadius[0] - x_center;
          ydelta = XYRadius[1] - y_center;
          radius = XYRadius[2];
          distance = (float)Math.sqrt( xdelta * xdelta + ydelta * ydelta );
          if( distance < radius )
          {
             ast_obj.Explode(GameObjectList);             
             InformationBar.me.Score += 10 * radius;
             SoundBox.me.Play(1);
             Destroy();
             break;
          }
          
       }
}
//---------------------------------------------------------------
public void Do()
{
  x_center += (float)(Math.cos(pi/180f * MoveAngle) * MAX_SPEED);
  y_center += (float)(Math.sin(pi/180f * MoveAngle) * MAX_SPEED);    
  
  LifeSpanCounter++;

    
  if( LifeSpanCounter > MAX_LIFE_SPAN_FRAMES )
      Destroy();      
      
  CheckCollideAsteroid();
}
//---------------------------------------------------------------
public void Draw( Graphics g )
{
   DrawOvalCenter( g, (int)x_center, (int)y_center, DIAMETER, DIAMETER );
}
//---------------------------------------------------------------
}
//###############################################################
class InformationBar extends GameObject
{

public int Score = 0;
public int Lives = 3;

public final static InformationBar me = new InformationBar();

//---------------------------------------------------------------
protected InformationBar()
{
}
//---------------------------------------------------------------
public void Draw( Graphics g ) 
{
  g.drawString("LIVES x " + Lives + "     SCORE " + Score, 30, 20 );
  
  if( Lives <= 0 )
      g.drawString( "GAME OVER", GameObject.ScreenWidth/2 - 50, GameObject.ScreenHeight/2 );
} 
//---------------------------------------------------------------
}
//###############################################################
class World
{
  public final static int MAX_GAME_OBJECT_COUNT = 500;
  protected static GameObject[] GameObjectList;
  
  public static final World me = new World();
//---------------------------------------------------------------  
  protected World()
  {
    int i;
    GameObjectList = new GameObject[MAX_GAME_OBJECT_COUNT];
    for( i = 0; i < MAX_GAME_OBJECT_COUNT; i++ )
         GameObjectList[i] = null;
    GameObject.ItsWorld = this;
  }
//---------------------------------------------------------------  
  public static void AddGameObject( GameObject obj )
  {
    int i;
    for( i = 0; i < MAX_GAME_OBJECT_COUNT; i++ )
      if( GameObjectList[i] == null )
      {
          GameObjectList[i] = obj;
          break;
      }
  }
//---------------------------------------------------------------  
  public static void AddGameObjectArray( GameObject[] objlist )
  {
    int i, length = objlist.length;
    
    for( i = 0; i < length; i++ )
         AddGameObject( objlist[i] );
  }
//---------------------------------------------------------------  
  public GameObject[] GetGameObjectList()
  {
     return GameObjectList;
  }
//---------------------------------------------------------------  
  public static void Do()
  {
    int i;
    for( i = 0; i < MAX_GAME_OBJECT_COUNT; i++ )
      if( GameObjectList[i] != null )
      {
        if( GameObjectList[i].ActiveFlag == 0 )        
           GameObjectList[i] = null;
        else
           GameObjectList[i].Do();           
      }
      
        GameObject.KeyPressDelayCount++;
        
        if( GameObject.KeyPressDelayCount >= GameObject.KeyPressDelayCountMax )
            GameObject.KeyPressDelayCount = GameObject.KeyPressDelayCountMax;              
  }
//---------------------------------------------------------------  
  public static void Draw( Graphics g )
  {
    int i;
    for( i = 0; i < MAX_GAME_OBJECT_COUNT; i++ )
      if( GameObjectList[i] != null )    
          GameObjectList[i].Draw(g);
          
    InformationBar.me.Draw(g);
  }
//---------------------------------------------------------------  
}
//###############################################################
class SoundBox
{

public static final SoundBox me = new SoundBox();
public static final int MAX_SOUND_COUNT = 4;

protected Clip[] ClipList;

//--------------------------------------------------------------- 
protected SoundBox()
{
  ClipList = new Clip[MAX_SOUND_COUNT];
  LoadClip( 0, "projectile.wav");
  LoadClip( 1, "breakup.wav");
  LoadClip( 2, "death.wav" );   
  LoadClip( 3, "warp.wav" );   
}
//--------------------------------------------------------------- 
public void LoadClip( int index, String filename )
{
try
{
   ClipList[index] = AudioSystem.getClip();
   File SoundFile =  new File(filename);
   
   if( !SoundFile.isFile())
   {
      ClipList[index] = null;
      return;
   }         
   AudioInputStream ais_obj = AudioSystem.getAudioInputStream(SoundFile);
   ClipList[index].open(ais_obj);
}
catch(Exception e)
{
}

}
//--------------------------------------------------------------- 
public void Play( int index )
{
   if( ClipList[index] != null )
       ClipList[index].loop(1);
}
//--------------------------------------------------------------- 
}
//###############################################################
//* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
//###############################################################  
public class asteroidsgame extends JApplet     
implements KeyListener, MouseListener, Runnable
{     
      int MouseX, MouseY, MouseState;
      int KeyCode;
      int ScreenWidth, ScreenHeight;
      int FontWidth, FontHeight;
      Thread MainThread;

      Random Rand = new Random();     

      BufferedImage GraphicsBuffer = null;       
      Graphics GraphicsObj;
      
//--------------------------------------------------------------- 
   
      public void init() 
      {       
       int i;              
             
       addKeyListener( this );
       addMouseListener( this );

       ScreenWidth = getSize().width;
       ScreenHeight = getSize().height;
       setBackground(Color.black);
       setForeground(Color.green);
       
       GameObject.ScreenWidth = ScreenWidth;
       GameObject.ScreenHeight = ScreenHeight;
       
       for( i = 0; i < 6; i++ )
       {
         World.me.AddGameObject( new Asteroid( 50 ));
       }
       World.me.AddGameObject( new Ship() );
       
       GraphicsBuffer = new BufferedImage(ScreenWidth, ScreenHeight, BufferedImage.TYPE_INT_RGB);
       GraphicsObj = GraphicsBuffer.createGraphics();
       
       FontWidth = FontHeight = 10;    
       setFocusable(true);
       MouseState = 0;
       KeyCode = 0;       
      }
//---------------------------------------------------------------  
  public void start()
  {
    MainThread = new Thread(this);
    MainThread.start();
  }
//---------------------------------------------------------------  
  public void run()
  {
    long FramesPerSecond = 30;         
    long TicksPerSecond = 1000 / FramesPerSecond;
    long StartTime;
    long SleepTime;
    long ActualSleepTime;
  
    while (MainThread != null ) 
    {    
      StartTime = System.currentTimeMillis();
      repaint();
      
      SleepTime = TicksPerSecond-(System.currentTimeMillis() - StartTime);
 
      if (SleepTime > 0)
          ActualSleepTime = SleepTime;
      else
          ActualSleepTime = 10;    
      try 
      {
        MainThread.sleep(ActualSleepTime);
      } 
      catch (InterruptedException e) 
      {
      }          
    }      
  }
//---------------------------------------------------------------  
  public void stop()
  {
     MainThread = null;
  }
//--------------------------------------------------------------- 
  public void update(Graphics g)
  {    
  }  
//---------------------------------------------------------------  
      public void paint(Graphics g)
      {       
         
         Font f = new Font("monospace", Font.PLAIN, 16);   
         
         GraphicsObj.setFont(f);          
         GraphicsObj.setColor(Color.black);
         GraphicsObj.fillRect(0,0,ScreenWidth,ScreenHeight);          
         GraphicsObj.setColor(new Color(100,100,255));         

         World.me.Do();
         World.me.Draw(GraphicsObj); 

         g.drawImage(GraphicsBuffer, 0, 0, this);
        
      }      
//---------------------------------------------------------------  
   public void keyPressed( KeyEvent e ) 
     { 
      char c = e.getKeyChar();
      int k = e.getKeyCode();
      KeyCode = k;

          if( KeyEvent.VK_UP == k )
              GameObject.vk_up = 1;
          else
          if( KeyEvent.VK_DOWN == k  )
              GameObject.vk_down = 1;
          else
          if( KeyEvent.VK_LEFT == k )
              GameObject.vk_left = 1;
          else 
          if( KeyEvent.VK_RIGHT == k )
              GameObject.vk_right = 1;             
          else 
          if( KeyEvent.VK_F == k )
              GameObject.vk_f = 1; 
          else 
          if( KeyEvent.VK_R == k )
              GameObject.vk_r = 1;               
       
          e.consume();  
     }
//---------------------------------------------------------------       
   public void keyReleased( KeyEvent e ) 
     {
      char c = e.getKeyChar();
      int k = e.getKeyCode();
      KeyCode = k;

          if( KeyEvent.VK_UP == k )
              GameObject.vk_up = 0;
          else
          if( KeyEvent.VK_DOWN == k  )
              GameObject.vk_down = 0;
          else
          if( KeyEvent.VK_LEFT == k )
              GameObject.vk_left = 0;
          else 
          if( KeyEvent.VK_RIGHT == k )
              GameObject.vk_right = 0;             
          else 
          if( KeyEvent.VK_F == k )
              GameObject.vk_f = 0; 
          else 
          if( KeyEvent.VK_R == k )
              GameObject.vk_r = 0;               
       
          e.consume();       
     }
//---------------------------------------------------------------       
   public void keyTyped( KeyEvent e ) 
   {
   }
//---------------------------------------------------------------  
   public void mouseEntered( MouseEvent e ) 
   { 
   }
//---------------------------------------------------------------     
   public void mouseExited( MouseEvent e ) 
   { 
   }
//--------------------------------------------------------------- 
   public void mouseMoved( MouseEvent e )
   {
   }   
//---------------------------------------------------------------
  public void mouseDragged(MouseEvent e) 
  {
  }     
//---------------------------------------------------------------  
   public void mousePressed( MouseEvent e ) 
   {
      MouseX = e.getX();
      MouseY = e.getY();
      MouseState = 1;
      e.consume(); 
   }
//---------------------------------------------------------------     
   public void mouseReleased( MouseEvent e ) 
   { 
      MouseX = e.getX();
      MouseY = e.getY();
      MouseState = 0;      
      e.consume();   
   }
//---------------------------------------------------------------     
   public void mouseClicked( MouseEvent e ) 
   {
   }       
//------------------------------------------------------   
} ///:~