hawk-ui
=======

Eclipse UI for MONDO/Hawk.

Instructions
============


In order to use these drivers with Hawk, follow these steps:

1. Clone the Hawk sources from:

		git clone https://github.com/kb634/mondo-hawk.git

2. Import the Hawk projects into an Eclipse workspace. Eclipse 4.3 (Kepler) is recommended if using the Modelio driver for Hawk. Ensure there are no compilation issues with Hawk and all dependencies are resolved.


3. Clone the Hawk UI plugins and import the drivers into the Eclipse workspace.

		git clone https://github.com/seyyedshah/hawk-ui.git

4. Create a new Eclipse run configuration, which contains the Hawk and Hawk UI projects, which are Eclipse OSGi plugins. 
