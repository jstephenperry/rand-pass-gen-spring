# Random Password Generator - Spring Shell

## Overview

This is a very simple password generator demo that should never be used for anything serious. This is just a demo of
something I wanted to test out with Spring Shell for the sake of learning and testing out emerging Java language
features.

## Simple Reference

This project contains just two shell commands that output straight to the command line:

* Generate a single password
* Generate a list of passwords

### Password Complexity Modes

There are only 3 password complexity modes (for the sake of simplicity and demonstration):

| Complexity Mode | Description                                                                 |
|-----------------|-----------------------------------------------------------------------------|
| LOW             | Generates an alpha only password string (upper and lower case)              |
| MEDIUM          | Generates an alphanumeric password string (all English letters and numbers) |
| HIGH            | Extends MEDIUM complexity to add special characters                         |

## Upcoming Enhancements

1. Add file output support for password list generation