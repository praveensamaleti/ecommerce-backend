@echo off
echo Checking for Python dependencies...
pip install -r requirements.txt

set /p num="Enter number of products to generate (default 100): "
if "%num%"=="" set num=100
python generate_products.py %num%
pause
