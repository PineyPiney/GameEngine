package com.pineypiney.game_engine.util

class NodeTree<T> {

	val nodes: MutableList<Node<T>> = mutableListOf()

	fun addElement(s: String, e: T, separator: Char = '.'){
		if(s.contains(separator)){
			// Follow the tree through s to find the right node, adding nodes if they don't exist yet
			val parts = s.split(separator)
			var node = get(parts[0]) ?: Node<T>(parts[0]).apply { nodes.add(this) }
			for(i in 1..parts.size-2){
				node = node[parts[i]] ?: Node<T>(parts[i]).apply { node.children.add(this) }
			}
			val lastNode = node[parts.last()]

			if(lastNode != null) {
				if(lastNode is ListNode) {
					lastNode.items.add(e)
					return
				}
				val replaceNode = if(lastNode is ItemNode) ListNode(parts.last(), mutableListOf(lastNode.item, e))
				else ItemNode(parts.last(), e)

				replaceNode.children.addAll(lastNode.children)
				lastNode.children.clear()
				node.children.remove(lastNode)
				node.children.add(replaceNode)
				return
			}
			node[parts.last()] = e
		}
		else{
			val lastNode = get(s)
			if(lastNode != null) {
				if(lastNode is ListNode) {
					lastNode.items.add(e)
					return
				}
				val replaceNode = if(lastNode is ItemNode) ListNode(s, mutableListOf(lastNode.item, e))
				else ItemNode(s, e)

				replaceNode.children.addAll(lastNode.children)
				lastNode.children.clear()
				nodes.remove(lastNode)
				nodes.add(replaceNode)
				return
			}
			nodes.add(ItemNode(s, e))
		}
	}
	operator fun get(id: String): Node<T>? = nodes.firstOrNull { it.id == id }

	/**
	 * Node containing no instances of T
	 */
	open class Node<T>(val id: String){
		val children = mutableListOf<Node<T>>()
		operator fun get(id: String): Node<T>? = children.firstOrNull { it.id == id }
		operator fun set(id: String, element: T) = children.add(ItemNode(id, element))
		override fun toString(): String = "Node($id)"
	}

	/**
	 * A node that contains one instance of T
	 */
	class ItemNode<T>(id: String, val item: T): Node<T>(id)

	/**
	 * A node containing a list of T
	 */
	class ListNode<T>(id: String, val items: MutableList<T>): Node<T>(id)

	companion object {
		fun <T> createFrom(map: Map<String, T>, separator: Char = '.'): NodeTree<T>{
			val tree = NodeTree<T>()
			for((s, e) in map){
				tree.addElement(s, e, separator)
			}
			return tree
		}
		fun <T> createFrom(list: List<T>, getter: T.() -> String, separator: Char = '.'): NodeTree<T>{
			val tree = NodeTree<T>()
			for(e in list){
				tree.addElement(e.getter(), e, separator)
			}
			return tree
		}
	}
}