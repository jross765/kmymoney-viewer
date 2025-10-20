# Notes on the Module "Viewer"

## What Does It Do? 

A Swing-based GUI viewer for 
KMyMoney 
XML-based files.

## Major Changes 
### V. 1.1
New.

Almost perfectly symmetric to sister module's V. 1.1.

## Planned

* Starting GUI with a specific account's or transaction's ID (and the viewer showing the according panel immediately).

  Analogously with all other entities that may be supported in future (cf. below).

* Copy marked object's ID into clipboard (context menu).

* Marking / rendering transactions by more general / flexible rules; extracting stuff like the "TODO" word into config files.

* Re-iterating tables and models -- I guess it would be better to handle transactions and transaction splits in completely separate classes (both in package `models` and `panels`) rather than in one class.

* Introducing detailed-view panels for each supported entity.

* Possibly (!) supporting additional entities:

  * Securities
  * Payees
  * Prices (low priority)

## Scope
Only partially / indirectly relevant here, but still...:

Have a look at the sister module's README file, section "Scope"; 
you cannot directly transfer e.t. there to the KMyMoney viewer, but nevertheless, it will
help you to understand where the viewer originally comes from and what it is supposed to do and what not.


## Known Issues

Veeery slow -- it takes some 30 s or so to load a larger real-life file (not the viewer itself, in fact, but the underlying API). 

This, in the current maintainer's opinion, is not so important for CLI based tools (cf. module "Tools"), and only partly relevant for a GUI (it takes long to load a file, but once it is loaded, e.t. runs fast and smoothly); but the above-mentioned calls for specific accounts / transactions (planned) would only partially make sense in a real-world scenario.
