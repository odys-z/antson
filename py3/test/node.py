from typing import get_type_hints

class Node:
    def __init__(self, value: int, next_node: 'Node' = None):
        self.value = value
        self.next_node = next_node

    def get_next(self) -> 'Node':
        return self.next_node

# Get type hints for the __init__ method
hints = get_type_hints(Node.__init__)
print(hints['next_node'])  # Outputs: <class '__main__.Node'>

node = Node(1)  # next_node is None by default
print(type(node.next_node))  # Outputs: <class 'NoneType'>

node2 = Node(1, node)  # Now next_node is another Node
print('node2.next_node', type(node2.next_node))