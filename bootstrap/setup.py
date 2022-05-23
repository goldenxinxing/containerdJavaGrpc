# Always prefer setuptools over distutils
from setuptools import setup, find_packages
import pathlib

here = pathlib.Path(__file__).parent.resolve()

# Get the long description from the README file
long_description = (here / "README.md").read_text(encoding="utf-8")

install_requires = [
    "click",
    "ansible-runner",
    "ansible"
]

# 说明：https://github.com/pypa/sampleproject/blob/main/setup.py
setup(
    name="swbootstrap",
    version="0.0.23",
    author="sw",
    author_email="15031259256@163.com",
    description="",
    long_description=long_description,
    long_description_content_type="text/markdown",
    packages=find_packages(where="."),  # Required
    include_package_data=True,
    install_requires=install_requires,
    entry_points="""
      [console_scripts]
      swbs = src.main:bootstrap
      bootstrap = src.main:bootstrap
      """,
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: Free for non-commercial use",
        "Operating System :: OS Independent",
    ]
)
