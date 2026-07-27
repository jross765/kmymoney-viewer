# Major Changes 

## V. 1.2 &rarr; 1.3
* I18N: Added French language.

* Generalized rendering of unbalanced and/or tagged transactions

  For now, this is based on transaction (split) comments, just as in the sister project, 
  not on the tag entity.

  **Note:** In theory, both transaction- and transaction-split-comments
  will define rendering. In practice, however, the former is only partially
  relevant, as it is not used by the orginal KMyMoney GUI. It does, however, exist,
  and it is filled by some tools (both published and unpublished ones).

## V. 1.1 &rarr; 1.2
* Viewer can now be started with command line options:
    * With account ID: Will open new window with according account immediatly after start.
    * With transaction split ID (or alternatively: account ID and transaction ID): 
      Will open new window with according account immediatly after start (as above with 
      account-ID only), and will, additionally, mark the according transaction (split).

* Copy marked object's ID into clipboard 
  (context menu for accounts, transactions and splits).

* Fixed a few small bugs.

* A couple of minor improvements, both on the surface and under the hood.

## V. 1.1
New.

Almost perfectly symmetric to sister module's V. 1.1.

## V. 1.0
Please note: Due to considerations about symmetry with the sister project,
this version *does not exist*.

Cf. the sister module's README file for more details.
