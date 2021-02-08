using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AStarPathfinding
{
    public class Location
    {
        public int X;
        public int Y;
        public double F;
        public int G;
        public double H;
        public Location Parent;
    }

    public struct Coordinate
    {
        public int y;
        public int x;

        public Coordinate(int y, int x)
        {
            this.x = x;
            this.y = y;
        }

        //Assuming a cell size of 32*32
        //The map has to start at 0,0 and have an XY orientation
        //The AStar class uses -YX coordinate system
        public Coordinate(Vector3 wCoord)
        {
            x = (int)wCoord.x;
            y = -((int)wCoord.y);
        }

        //Give back the center of a tile
        public Vector3 toVector3()
        {
            float wx = x + 0.5f;
            float wy = -y + 0.5f;
            return new Vector3(wx, wy, 0);
        }

        public Vector2 toVector2()
        {
            float wx = x + 0.5f;
            float wy = -y + 0.5f;
            return new Vector2(wx, wy);
        }


    }

    public class AStar
    {
        public static List<Coordinate> findPath(char[,] map, Coordinate from, Coordinate to)
        {
            // algorithm

            Location current = null;
            var start = new Location {  Y = from.y, X = from.x };
            var target = new Location { Y = to.y, X = to.x };
            var openList = new List<Location>();
            var closedList = new List<Location>();
            int g = 0;
            int maxIteration = 999;

            map[from.y, from.x] = 'A';
            map[to.y, to.x] = 'B';

            // start by adding the original position to the open list
            openList.Add(start);

            for (int iteration = 0; openList.Count > 0; iteration++)
            {
                // get the square with the lowest F score
                var lowest = openList.Min(l => l.F);
                current = openList.First(l => l.F == lowest);

                // add the current square to the closed list
                closedList.Add(current);

                // remove it from the open list
                openList.Remove(current);

                // if we added the destination to the closed list, we've found a path
                if (closedList.FirstOrDefault(l => l.X == target.X && l.Y == target.Y) != null)
                    break;

                var adjacentSquares = GetWalkableAdjacentSquares(current.X, current.Y, map);
                g++;

                foreach (var adjacentSquare in adjacentSquares)
                {
                    // if this adjacent square is already in the closed list, ignore it
                    if (closedList.FirstOrDefault(l => l.X == adjacentSquare.X
                            && l.Y == adjacentSquare.Y) != null)
                        continue;

                    // if it's not in the open list...
                    if (openList.FirstOrDefault(l => l.X == adjacentSquare.X
                            && l.Y == adjacentSquare.Y) == null)
                    {
                        // compute its score, set the parent
                        adjacentSquare.G = g;
                        adjacentSquare.H = ComputeHScore(adjacentSquare.X, adjacentSquare.Y, target.X, target.Y);
                        adjacentSquare.F = adjacentSquare.G + adjacentSquare.H;
                        adjacentSquare.Parent = current;

                        // and add it to the open list
                        openList.Insert(0, adjacentSquare);
                    }
                    else
                    {
                        // test if using the current G score makes the adjacent square's F score
                        // lower, if yes update the parent because it means it's a better path
                        if (g + adjacentSquare.H < adjacentSquare.F)
                        {
                            adjacentSquare.G = g;
                            adjacentSquare.F = adjacentSquare.G + adjacentSquare.H;
                            adjacentSquare.Parent = current;
                        }
                    }
                }
                if (iteration >= maxIteration) return null;
            }

            var resultList = new List<Coordinate>();

            while (current != null)
            {
                Coordinate c = new Coordinate(current.Y, current.X);
                resultList.Add(c);

                current = current.Parent;
            }

            //resultList.Reverse();

            return resultList;
        }

        static List<Location> GetWalkableAdjacentSquares(int x, int y, char[,] map)
        {
            var proposedLocations = new List<Location>()
            {
                new Location { X = x, Y = y - 1 },
                new Location { X = x, Y = y + 1 },
                new Location { X = x - 1, Y = y },
                new Location { X = x + 1, Y = y },

                //Diagonal
                new Location { X = x - 1, Y = y - 1 },
                new Location { X = x + 1, Y = y + 1 },
                new Location { X = x - 1, Y = y + 1 },
                new Location { X = x + 1, Y = y - 1 },
            };

            return proposedLocations.Where(l => l.X >= 0 && l.X < map.GetLength(1) && l.Y >= 0 &&l.Y < map.GetLength(0) &&
            (map[l.Y, l.X] == ' ' || map[l.Y, l.X] == 'B')).ToList();
        }

        static double ComputeHScore(int x, int y, int targetX, int targetY)
        {
            return Math.Sqrt(Math.Pow((double) targetX - x, 2.0) + Math.Pow((double) targetY - y, 2.0));
        }
    }
}
