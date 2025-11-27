# Notes on the Module "Viewer"

## What Does It Do?

A Swing-based GUI viewer for 
KMyMoney 
XML-based files.

## What is This Repo's Relationship with the Other Repos?

* This is a module-level repository which is part of a multi-module project, i.e. it has a parent and several siblings. 

  [Parent](https://github.com/jross765/JKMyMoneyLibNTools.git)

* Under normal circumstances, you cannot compile it on its own (at least not without further preparation), but instead, you should clone it together with the other repos and use the parent repo's build-script.

## Major Changes 
### V. 1.1 &rarr; 1.2
* Viewer can now be started with command line options:
    * With account ID: Will open new window with according account immediatly after start.
    * With transaction split ID (or alternatively: account ID and transaction ID): Will open new window with according account immediatly after start (as above with account-ID only), and in addition to that
mark the according transaction (split).

* Fixed a few small bugs.

* A couple of minor improvements, both on the surface and under the hood.

### V. 1.1
New.

Almost perfectly symmetric to sister module's V. 1.1.

## Planned

* Copy marked object's ID into clipboard (context menu).

* Marking / rendering transactions by more general / flexible rules; extracting stuff like the "TODO" word into config files.

* Re-iterating tables and models -- I guess it would be better to handle transactions and transaction splits in completely separate classes (both in package `models` and `panels`) rather than in one class.

* Introducing detailed-view panels for each supported entity.

* Possibly (!) supporting additional entities:

  * Securities
  * Payees
  * Prices (low priority)

* Starting GUI with above-mentioned new entities, the viewer showing the according panel immediately (analogous to accounts, transactions and transaction splits).

## Scope
Only partially / indirectly relevant here, but still...:

Have a look at the sister module's README file, section "Scope"; 
you cannot directly transfer e.t. there to the KMyMoney viewer, but nevertheless, it will
help you to understand where the viewer originally comes from and what it is supposed to do and what not.


## Known Issues

A bit slow -- it takes some 40 s or so to load the current maintainer's personal finance file (not the viewer itself, in fact, but the underlying API). 

This, in the current maintainer's opinion, is not so important for CLI based tools (cf. module "Tools"), and only partly relevant for a GUI (it takes long to load a file, but once it is loaded, e.t. runs fast and smoothly); but calls for specific accounts / transaction (splits) (introduced in V. 1.2) only partially make sense in a real-world scenario; few persons will be willing to wait that long for "quick glimpse".
