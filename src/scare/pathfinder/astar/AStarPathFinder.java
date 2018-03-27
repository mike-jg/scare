package scare.pathfinder.astar;

import java.util.ArrayList;
import java.util.Collections;
import scare.pathfinder.Mover;
import scare.pathfinder.Path;
import scare.pathfinder.PathFinder;
import scare.pathfinder.TileMap;


/**
 * Copyright (c) 2013, Slick2D
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * Neither the name of the Slick2D nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class AStarPathFinder implements PathFinder
{

    /**
     * nodes that have been searched through
     */
    private ArrayList<Node> closed = new ArrayList<>();
    /**
     * nodes to be searched
     */
    private SortedList<Node> open = new SortedList<>();
    /**
     * map to search
     */
    private TileMap map;
    /**
     * max distance to search before giving up
     */
    private int maxSearchDistance;
    /**
     * complete set of nodes across the map
     */
    private Node[][] nodes;
    /**
     *
     */
    private boolean allowDiagonalMovement;
    /**
     * The heuristic which decides which nodes to search first
     */
    private AStarHeuristic heuristic;

    public AStarPathFinder(TileMap map, int maxSearchDistance)
    {
        this(map, maxSearchDistance, true, new ClosestHeuristic());
    }

    public AStarPathFinder(TileMap map, int maxSearchDistance, boolean allowDiagonalMovement, AStarHeuristic heuristic)
    {
        this.map = map;
        this.maxSearchDistance = maxSearchDistance;
        this.allowDiagonalMovement = allowDiagonalMovement;
        this.heuristic = heuristic;

        nodes = new Node[map.getWidthInTiles()][map.getHeightInTiles()];
        for (int x = 0; x < map.getWidthInTiles(); x++) {
            for (int y = 0; y < map.getHeightInTiles(); y++) {
                nodes[x][y] = new Node(x, y);
            }
        }
    }

    @Override
    /**
     * @see PathFinder#findPath(Mover, int, int, int, int)
     */
    public Path findPath(Mover mover, int sx, int sy, int tx, int ty)
    {
        // easy first check, if the destination is blocked, we can't get there

        if (map.blocked(mover, tx, ty)) {
            return null;
        }

        // initial state for A*. The closed group is empty. Only the starting
        // tile is in the open list and it'e're already there
        nodes[sx][sy].cost = 0;
        nodes[sx][sy].depth = 0;
        closed.clear();
        open.clear();
        open.add(nodes[sx][sy]);

        nodes[tx][ty].parent = null;

        // while we havenn't exceeded our max search depth
        int maxDepth = 0;
        while ((maxDepth < maxSearchDistance) && (open.size() != 0)) {
            // pull out the first node in our open list, this is determined to
            // be the most likely to be the next step based on our heuristic

            Node current = getFirstInOpen();
            if (current == nodes[tx][ty]) {
                break;
            }

            removeFromOpen(current);
            addToClosed(current);

            // search through all the neighbours of the current node evaluating
            // them as next steps
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    // not a neighbour, it's the current tile
                    if ((x == 0) && (y == 0)) {
                        continue;
                    }

                    // if we're not allowing diaganol movement then only
                    // one of x or y can be set
                    if (!allowDiagonalMovement) {
                        if ((x != 0) && (y != 0)) {
                            continue;
                        }
                    }

                    // determine the location of the neighbour and evaluate it
                    int xp = x + current.x;
                    int yp = y + current.y;

                    if (isValidLocation(mover, sx, sy, xp, yp)) {
                        // the cost to get to this node is cost the current plus the movement
                        // cost to reach this node. Note that the heursitic value is only used
                        // in the sorted open list

                        float nextStepCost = current.cost + getMovementCost(mover, current.x, current.y, xp, yp);
                        Node neighbour = nodes[xp][yp];
                        map.pathFinderVisited(xp, yp);

                        // if the new cost we've determined for this node is lower than
                        // it has been previously makes sure the node hasn'e've
                        // determined that there might have been a better path to get to
                        // this node so it needs to be re-evaluated
                        if (nextStepCost < neighbour.cost) {
                            if (inOpenList(neighbour)) {
                                removeFromOpen(neighbour);
                            }
                            if (inClosedList(neighbour)) {
                                removeFromClosed(neighbour);
                            }
                        }

                        // if the node hasn't already been processed and discarded then
                        // reset it's cost to our current cost and add it as a next possible
                        // step (i.e. to the open list)
                        if (!inOpenList(neighbour) && !(inClosedList(neighbour))) {
                            neighbour.cost = nextStepCost;
                            neighbour.heuristic = getHeuristicCost(mover, xp, yp, tx, ty);
                            maxDepth = Math.max(maxDepth, neighbour.setParent(current));
                            addToOpen(neighbour);
                        }
                    }
                }
            }
        }

        // since we've run out of search
        // there was no path. Just return null
        if (nodes[tx][ty].parent == null) {
            return null;
        }

        // At this point we've definitely found a path so we can uses the parent
        // references of the nodes to find out way from the target location back
        // to the start recording the nodes on the way.
        Path path = new Path();
        Node target = nodes[tx][ty];
        while (target != nodes[sx][sy]) {
            path.prependStep(target.x, target.y);
            target = target.parent;
        }
        path.prependStep(sx, sy);

        return path;
    }

    /**
     * Get the first element from the open list. This is the next one to be
     * searched.
     *
     * @return The first element in the open list
     */
    private Node getFirstInOpen()
    {
        return (Node) open.first();
    }

    /**
     * Add a node to the open list
     *
     * @param node The node to be added to the open list
     */
    private void addToOpen(Node node)
    {
        open.add(node);
    }

    /**
     * Check if a node is in the open list
     *
     * @param node The node to check for
     * @return True if the node given is in the open list
     */
    private boolean inOpenList(Node node)
    {
        return open.contains(node);
    }

    /**
     * Remove a node from the open list
     *
     * @param node The node to remove from the open list
     */
    private void removeFromOpen(Node node)
    {
        open.remove(node);
    }

    /**
     * Add a node to the closed list
     *
     * @param node The node to add to the closed list
     */
    private void addToClosed(Node node)
    {
        closed.add(node);
    }

    /**
     * Check if the node supplied is in the closed list
     *
     * @param node The node to search for
     * @return True if the node specified is in the closed list
     */
    private boolean inClosedList(Node node)
    {
        return closed.contains(node);
    }

    /**
     * Remove a node from the closed list
     *
     * @param node The node to remove from the closed list
     */
    private void removeFromClosed(Node node)
    {
        closed.remove(node);
    }

    /**
     * Check if a given location is valid for the supplied mover
     *
     * @param mover The mover that would hold a given location
     * @param sx The starting x coordinate
     * @param sy The starting y coordinate
     * @param x The x coordinate of the location to check
     * @param y The y coordinate of the location to check
     * @return True if the location is valid for the given mover
     */
    protected boolean isValidLocation(Mover mover, int sx, int sy, int x, int y)
    {
        boolean invalid = (x < 0) || (y < 0) || (x >= map.getWidthInTiles()) || (y >= map.getHeightInTiles());

        if ((!invalid) && ((sx != x) || (sy != y))) {
            invalid = map.blocked(mover, x, y);
        }

        return !invalid;
    }

    /**
     * Get the cost to move through a given location
     *
     * @param mover The entity that is being moved
     * @param sx The x coordinate of the tile whose cost is being determined
     * @param sy The y coordiante of the tile whose cost is being determined
     * @param tx The x coordinate of the target location
     * @param ty The y coordinate of the target location
     * @return The cost of movement through the given tile
     */
    public float getMovementCost(Mover mover, int sx, int sy, int tx, int ty)
    {
        return map.getCost(mover, sx, sy, tx, ty);
    }

    /**
     * Get the heuristic cost for the given location. This determines in which
     * order the locations are processed.
     *
     * @param mover The entity that is being moved
     * @param x The x coordinate of the tile whose cost is being determined
     * @param y The y coordiante of the tile whose cost is being determined
     * @param tx The x coordinate of the target location
     * @param ty The y coordinate of the target location
     * @return The heuristic cost assigned to the tile
     */
    public float getHeuristicCost(Mover mover, int x, int y, int tx, int ty)
    {
        return heuristic.getCost(map, mover, x, y, tx, ty);
    }

    private class SortedList<T extends Comparable>
    {

        private ArrayList<T> list = new ArrayList<>();

        public T first()
        {
            return list.get(0);
        }

        public void clear()
        {
            list.clear();
        }

        public void add(T o)
        {
            list.add(o);
            Collections.sort(list);
        }

        public void remove(T o)
        {
            list.remove(o);
        }

        public int size()
        {
            return list.size();
        }

        public boolean contains(T o)
        {
            return list.contains(o);
        }
    }

    private class Node implements Comparable
    {

        /**
         * The x coordinate of the node
         */
        private int x;
        /**
         * The y coordinate of the node
         */
        private int y;
        /**
         * The path cost for this node
         */
        private float cost;
        /**
         * The parent of this node, how we reached it in the search
         */
        private Node parent;
        /**
         * The heuristic cost of this node
         */
        private float heuristic;
        /**
         * The search depth of this node
         */
        private int depth;

        /**
         * Create a new node
         *
         * @param x The x coordinate of the node
         * @param y The y coordinate of the node
         */
        public Node(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        /**
         * Set the parent of this node
         *
         * @param parent The parent node which lead us to this node
         * @return The depth we have no reached in searching
         */
        public int setParent(Node parent)
        {
            depth = parent.depth + 1;
            this.parent = parent;

            return depth;
        }

        /**
         * @see Comparable#compareTo(Object)
         */
        @Override
        public int compareTo(Object other)
        {
            Node o = (Node) other;

            float f = heuristic + cost;
            float of = o.heuristic + o.cost;

            if (f < of) {
                return -1;
            }
            else if (f > of) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }
}
